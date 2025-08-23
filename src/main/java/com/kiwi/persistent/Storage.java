package com.kiwi.persistent;

import com.kiwi.dto.DataRequest;
import com.kiwi.persistent.dto.Key;
import com.kiwi.persistent.dto.Value;
import java.util.HashMap;
import java.util.Map;

public class Storage {
    private final Map<Key, Value> inMemoryStorage = new HashMap<>();

    public void save(DataRequest request) {
        inMemoryStorage.put(request.key(), request.data());
    }

    public Value getData(Key key) {
        //TODO null check?
        return inMemoryStorage.get(key);
    }

    public void delete(Key key) {
        inMemoryStorage.remove(key);
    }
}
