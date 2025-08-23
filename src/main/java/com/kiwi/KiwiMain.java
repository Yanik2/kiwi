package com.kiwi;

import com.kiwi.persistent.PersistentModule;
import com.kiwi.processor.ProcessorModule;
import com.kiwi.server.factory.ServerModule;
import java.io.IOException;
import java.util.logging.Logger;

public class KiwiMain {
    private static final Logger log = Logger.getLogger(KiwiMain.class.getName());

    public static void main(String[] args) throws IOException {
        log.info("Starting Kiwi");
        final var storage = PersistentModule.create();
        final var dataProcessor = ProcessorModule.create(storage);
        final var server = ServerModule.create(dataProcessor);
        server.start();
    }
}
