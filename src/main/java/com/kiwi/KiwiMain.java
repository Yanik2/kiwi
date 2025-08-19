package com.kiwi;

import com.kiwi.persistent.PersistentModule;
import com.kiwi.processor.ProcessorModule;
import com.kiwi.server.factory.ServerModule;
import java.io.IOException;

public class KiwiMain {
    public static void main(String[] args) throws IOException {
        final var storage = PersistentModule.create();
        final var dataProcessor = ProcessorModule.create(storage);
        final var server = ServerModule.create(dataProcessor);
        server.start();
    }
}
