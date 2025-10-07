package com.kiwi.persistent;

import com.kiwi.persistent.dto.StorageRequest;
import com.kiwi.persistent.model.Key;
import com.kiwi.persistent.model.Value;
import java.util.HashMap;
import java.util.Map;

public class Storage {
    private final Map<Key, Value> inMemoryStorage = new HashMap<>();

    public void save(StorageRequest request) {
        inMemoryStorage.put(request.key(), request.value());
    }

    public Value getData(StorageRequest request) {
        //TODO null check?
        return inMemoryStorage.get(request.key());
    }

    public void delete(StorageRequest request) {
        inMemoryStorage.remove(request.key());
    }
}
