package com.kiwi.processor;

import com.kiwi.dto.DataRequest;
import com.kiwi.persistent.Storage;

public class DataProcessor {
    private final Storage storage;

    public DataProcessor(Storage storage) {
        this.storage = storage;
    }

    public void processData(DataRequest request) {
        storage.save(request);
    }

    public byte[] getValue(String key) {
        return storage.getData(key);
    }

    public void deleteValue(String key) {
        storage.delete(key);
    }
}
