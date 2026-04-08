package com.kiwi.persistent;

import com.kiwi.observability.StorageMetrics;
import com.kiwi.persistent.model.Key;
import com.kiwi.persistent.model.Value;
import com.kiwi.persistent.mutation.CurrentState;
import com.kiwi.persistent.mutation.Mutation;
import com.kiwi.persistent.mutation.MutationDecision;
import com.kiwi.persistent.mutation.MutationResult;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class StorageFacade {

    private final Map<Key, Value> inMemoryStorage = new HashMap<>();
    private final Lock lock = new ReentrantLock();

    private final StorageMetrics storageMetrics;

    public StorageFacade(StorageMetrics storageMetrics) {
        this.storageMetrics = storageMetrics;
    }

    public Optional<Value> read(Key key) {
        lock.lock();
        try {
            return expirationGate(key);
        } finally {
            lock.unlock();
        }
    }

    public void write(Key key, Value value) {
        lock.lock();
        try {
            inMemoryStorage.put(key, value);
        } finally {
            lock.unlock();
        }
    }

    public MutationResult mutate(Key key, Mutation mutation) {
        lock.lock();
        try {
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
}
