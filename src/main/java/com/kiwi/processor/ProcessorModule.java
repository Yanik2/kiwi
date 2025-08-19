package com.kiwi.processor;

import com.kiwi.persistent.Storage;

public class ProcessorModule {
    public static DataProcessor create(Storage storage) {
        return new DataProcessor(storage);
    }
}
