package com.kiwi.persistent.storage;

import com.kiwi.observability.StorageMetrics;
import com.kiwi.persistent.model.Key;
import com.kiwi.persistent.model.Value;
import com.kiwi.persistent.mutation.CurrentState;
import com.kiwi.persistent.mutation.Mutation;
import com.kiwi.persistent.mutation.MutationDecision;
import com.kiwi.persistent.mutation.MutationResult;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class StorageCoarseLockImpl implements Storage {

    private final Map<Key, Value> inMemoryStorage = new HashMap<>();
    private final Lock lock = new ReentrantLock();

    private final StorageMetrics storageMetrics;

    public StorageCoarseLockImpl(StorageMetrics storageMetrics) {
        this.storageMetrics = storageMetrics;
    }

    public Optional<Value> read(Key key) {
        lock.lock();
        try {
            longOperation();
            return expirationGate(key);
        } finally {
            lock.unlock();
        }
    }

    public void write(Key key, Value value) {
        lock.lock();
        try {
            longOperation();
            inMemoryStorage.put(key, value);
        } finally {
            lock.unlock();
        }
    }

    public MutationResult mutate(Key key, Mutation mutation) {
        lock.lock();
        try {
            longOperation();
            final var value = expirationGate(key);
            final var state = new CurrentState(value.isPresent(), value.orElse(null));
            final var mutationDecision = mutation.apply(state);
            return switch (mutationDecision) {
                case MutationDecision.Write w -> {
                    inMemoryStorage.put(key, w.value());
                    yield new MutationResult(key, Optional.ofNullable(w.returnValue()), w.success());
                }
                case MutationDecision.Delete d -> {
                    inMemoryStorage.remove(key);
                    yield new MutationResult(key, Optional.empty(), d.success());
                }
                case MutationDecision.NoOp n -> new MutationResult(key, Optional.empty(), n.success());
                case MutationDecision.Error e -> new MutationResult(key, Optional.of(
                        new Value(e.errorType().name().getBytes())
                ), false);
            };
        } finally {
            lock.unlock();
        }
    }

    public void delete(Key key) {
        lock.lock();
        try {
            longOperation();
            final var value = inMemoryStorage.get(key);
            if (value != null && value.getExpiryPolicy().shouldEvictOnRead(System.currentTimeMillis())) {
                storageMetrics.onTtlExpiredEviction();
            }
            inMemoryStorage.remove(key);
        } finally {
            lock.unlock();
        }
    }

    public int size() {
        lock.lock();
        try {
            longOperation();
            int size = inMemoryStorage.size();
            final var keyList = new LinkedList<>(inMemoryStorage.keySet());
            for (Key k : keyList) {
                if (expirationGate(k).isEmpty()) {
                    size--;
                }
            }

            return size;
        } finally {
            lock.unlock();
        }
    }

    private Optional<Value> expirationGate(Key key) {
        final var value = inMemoryStorage.get(key);
        if (value == null) {
            return Optional.empty();
        }

        if (value.getExpiryPolicy().shouldEvictOnRead(System.currentTimeMillis())) {
            inMemoryStorage.remove(key);
            storageMetrics.onTtlExpiredEviction();
            return Optional.empty();
        }

        return Optional.of(value);
    }

    private void longOperation() {
        long x = System.nanoTime();

        for (int i = 0; i < 100000; i++) {
            // Some meaningless but real CPU work
            x ^= (x << 13);
            x ^= (x >>> 7);
            x ^= (x << 17);
            x += i * 31L;
        }

        sink = x;
    }
    private static volatile long sink;
}
