package com.kiwi.persistent;

import com.kiwi.dto.DataRequest;
import java.util.HashMap;
import java.util.Map;

public class Storage {
    private final Map<String, byte[]> inMemoryStorage = new HashMap<>();

    public void save(DataRequest request) {
        inMemoryStorage.put(request.key(), request.data());
    }

    public byte[] getData(String key) {
        //TODO null check?
        return inMemoryStorage.get(key);
    }

    public void delete(String key) {
        inMemoryStorage.remove(key);
    }
}
