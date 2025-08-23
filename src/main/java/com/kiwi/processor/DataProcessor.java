package com.kiwi.processor;

import com.kiwi.dto.DataRequest;
import com.kiwi.persistent.Storage;
import com.kiwi.persistent.dto.Key;
import com.kiwi.persistent.dto.Value;

public class DataProcessor {
    private final Storage storage;

    public DataProcessor(Storage storage) {
        this.storage = storage;
    }

    public void processData(DataRequest request) {
        storage.save(request);
    }

    public Value getValue(Key key) {
        return storage.getData(key);
    }

    public void deleteValue(Key key) {
        storage.delete(key);
    }
}
