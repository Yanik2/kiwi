package com.kiwi.log;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

public class KiwiLoggerFactory {
    private static final Executor executor;

    static {
        executor = Executors.newSingleThreadExecutor(r -> {
            final var thread = new Thread(r);
            thread.setDaemon(true);
            return thread;
        });
    }

    private static final Logger log = Logger.getLogger("KiwiLogger");

    private KiwiLoggerFactory() {
    }

    public static KiwiLogger getLogger(String name) {
        final var logger = new KiwiLogger(executor, name, log);
        final var registry = KiwiLoggerRegistry.getInstance();
        return registry.addLogger(name, logger);
    }
}
