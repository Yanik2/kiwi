package com.kiwi.persistent;

import com.kiwi.observability.StorageMetrics;
import com.kiwi.persistent.dto.StorageRequest;
import com.kiwi.persistent.model.Key;
import com.kiwi.persistent.model.Value;
import java.util.HashMap;
import java.util.Map;

public class Storage {
    private final Map<Key, Value> inMemoryStorage = new HashMap<>();

    private final StorageMetrics storageMetrics;

    public Storage(StorageMetrics storageMetrics) {
        this.storageMetrics = storageMetrics;
    }

    public void save(StorageRequest request) {
        inMemoryStorage.put(request.key(), request.value());
    }

    public Value getData(StorageRequest request) {
        final var value = inMemoryStorage.get(request.key());

        if (value == null || value.getExpiryPolicy().shouldEvictOnRead(System.currentTimeMillis())) {
            inMemoryStorage.remove(request.key());
            storageMetrics.onTtlExpiredEviction();
            return new Value(new byte[0]);
        } else {
            return value;
        }
    }

    public void delete(StorageRequest request) {
        final var value = inMemoryStorage.get(request.key());
        // TODO will be implemented later with permanent storage lookup only for headers
        if (value != null && value.getExpiryPolicy().shouldEvictOnRead(System.currentTimeMillis())) {
            storageMetrics.onTtlExpiredEviction();
        }
        inMemoryStorage.remove(request.key());
    }
}
