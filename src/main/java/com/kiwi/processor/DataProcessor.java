package com.kiwi.processor;

import com.kiwi.dto.DataRequest;
import com.kiwi.persistent.Storage;
import com.kiwi.persistent.dto.StorageRequest;
import com.kiwi.persistent.model.Key;
import com.kiwi.persistent.model.Value;
import com.kiwi.persistent.model.expiration.NoOpExpiration;

public class DataProcessor {
    private final Storage storage;

    public DataProcessor(Storage storage) {
        this.storage = storage;
    }

    public void setData(DataRequest request) {
        final var key = new Key(request.key());
        final var value = new Value(request.value(), NoOpExpiration.getInstance());

        storage.save(new StorageRequest(key, value));
    }

    public Value getValue(DataRequest request) {
        final var storageRequest = new StorageRequest(new Key(request.key()));
        final var value = storage.getData(storageRequest);
        if (value.getExpiryPolicy().shouldEvictOnRead(System.currentTimeMillis())) {
            storage.delete(storageRequest);
        }
        return value;
    }

    public void deleteValue(DataRequest request) {
        storage.delete(new StorageRequest(new Key(request.key())));
    }
}
