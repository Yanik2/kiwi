package com.kiwi.persistent;

import com.kiwi.observability.StorageMetrics;
import com.kiwi.persistent.dto.StorageRequest;
import com.kiwi.persistent.model.Key;
import com.kiwi.persistent.model.Value;
import com.kiwi.persistent.model.expiration.ExpiryPolicy;
import com.kiwi.persistent.model.expiration.NoOpExpiration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class Storage {
    private static final long TTL_RESPONSE_NOT_FOUND = -2L;
    private static final long TTL_RESPONSE_NO_TTL = -1L;

    private final Map<Key, Value> inMemoryStorage = new HashMap<>();

    private final StorageMetrics storageMetrics;

    public Storage(StorageMetrics storageMetrics) {
        this.storageMetrics = storageMetrics;
    }

    public void save(StorageRequest request) {
        inMemoryStorage.put(request.key(), request.value());
    }

    public Value getData(StorageRequest request) {
        return expirationGate(request.key()).orElseGet(() -> new Value(new byte[0]));
    }

    public void delete(StorageRequest request) {
        final var value = inMemoryStorage.get(request.key());
        // TODO will be implemented later with permanent storage lookup only for headers
        if (value != null && value.getExpiryPolicy().shouldEvictOnRead(System.currentTimeMillis())) {
            storageMetrics.onTtlExpiredEviction();
        }
        inMemoryStorage.remove(request.key());
    }

    public boolean updateExpiration(Key key, ExpiryPolicy expiryPolicy) {
        final var value = expirationGate(key);
        if (value.isEmpty()) {
            return false;
        }

        if (expiryPolicy.hasTtl()) {
            if (expiryPolicy.remainingTime(System.currentTimeMillis()) <= 0) {
                inMemoryStorage.remove(key);
                return false;
            }
        }

        value.get().setExpiryPolicy(expiryPolicy);
        return true;
    }

    public boolean persist(Key key) {
        final var optionalValue = expirationGate(key);
        if (optionalValue.isEmpty()) {
            return false;
        }

        final var value = optionalValue.get();

        if (value.getExpiryPolicy().hasTtl()) {
            value.setExpiryPolicy(NoOpExpiration.getInstance());
            return true;
        } else {
            return false;
        }
    }

    public long getTtl(Key key) {
        final var value = expirationGate(key);
        if (value.isEmpty()) {
            return TTL_RESPONSE_NOT_FOUND;
        }

        if (value.get().getExpiryPolicy().hasTtl()) {
            return value.get().getExpiryPolicy().remainingTime(System.currentTimeMillis());
        } else {
            return TTL_RESPONSE_NO_TTL;
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
