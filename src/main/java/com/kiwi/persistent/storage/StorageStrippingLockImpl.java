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

public class StorageStrippingLockImpl implements Storage {
    private final StorageMetrics storageMetrics;

    private final Map<Key, Value> inMemoryStorage = new HashMap<>();
    private volatile ReentrantLock[] locks;
    private final ReentrantLock generalLock = new ReentrantLock();

    private volatile boolean resizeInProgress = false;

    public StorageStrippingLockImpl(StorageMetrics storageMetrics) {
        this.storageMetrics = storageMetrics;
        this.locks = new ReentrantLock[16];
        for (int i = 0; i < 16; i++) {
            locks[i] = new ReentrantLock();
        }
    }

    @Override
    public Optional<Value> read(Key key) {
        final var lock = locks[Math.abs(key.hashCode() % locks.length)];
        lock.lock();
        try {
            longOperation();
            return expirationGate(key);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void write(Key key, Value value) {
        while (resizeInProgress) {}
        resizeLocks();
        final var lock = locks[Math.abs(key.hashCode() % locks.length)];
        lock.lock();
        try {
            longOperation();
            inMemoryStorage.put(key, value);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public MutationResult mutate(Key key, Mutation mutation) {
        while (resizeInProgress) {}
        resizeLocks();
        final var lock = locks[Math.abs(key.hashCode() % locks.length)];
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

    @Override
    public void delete(Key key) {
        while (resizeInProgress) {}
        final var lock = locks[Math.abs(key.hashCode() % locks.length)];
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

    @Override
    public int size() {
        generalLock.lock();
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
            generalLock.unlock();
        }
    }

    private void resizeLocks() {
        if (inMemoryStorage.size() / 4 < locks.length && inMemoryStorage.size() > locks.length) {
            return;
        }

        if (generalLock.tryLock()) {
            try {
                this.resizeInProgress = true;
                final var newLocks = new ReentrantLock[Math.max(inMemoryStorage.size() / 2, 16)];
                final var oldLocks = this.locks;
                for (Lock l : oldLocks) {
                    l.lock();
                }

                for (int i = 0; i < newLocks.length; i++) {
                    newLocks[i] = new ReentrantLock();
                }

                this.locks = newLocks;
                this.resizeInProgress = false;

                for (Lock l : oldLocks) {
                    l.unlock();
                }
            } finally {
                generalLock.unlock();
            }
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
