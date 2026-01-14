package com.kiwi;

import com.kiwi.concurrency.ConcurrencyModule;
import com.kiwi.persistent.PersistentModule;
import com.kiwi.server.factory.ServerModule;
import java.io.IOException;
import java.util.logging.Logger;

public class KiwiMain {
    private static final Logger log = Logger.getLogger(KiwiMain.class.getName());

    public static void main(String[] args) throws IOException {
        log.info("Starting initialization Kiwi");
        final long start = System.currentTimeMillis();
        ConcurrencyModule.init();
        final var storage = PersistentModule.create();
        //TODO size of thread pool will be in properties
        final var serverThreadPool = ConcurrencyModule.createExecutor("server-executor", "server-thread-pool", 1, 1);
        final var server = ServerModule.create(storage, serverThreadPool);
        final long end = System.currentTimeMillis();
        log.info("Kiwi initialized in [" + (end - start) + "]ms, starting server");
        server.start();
    }
}
