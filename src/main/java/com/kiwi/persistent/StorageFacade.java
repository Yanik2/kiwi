package com.kiwi.persistent;

import com.kiwi.observability.StorageMetrics;
import com.kiwi.persistent.model.Key;
import com.kiwi.persistent.model.Value;
import com.kiwi.persistent.mutation.CurrentState;
import com.kiwi.persistent.mutation.Mutation;
import com.kiwi.persistent.mutation.MutationResult;

import java.util.HashMap;
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
            return mutationDecision.applyDecision(inMemoryStorage, key);
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
