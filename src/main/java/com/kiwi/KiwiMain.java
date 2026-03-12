package com.kiwi;

import com.kiwi.concurrency.factory.ConcurrencyModule;
import com.kiwi.observability.factory.ObservabilityModule;
import com.kiwi.persistent.factory.PersistentModule;
import com.kiwi.server.factory.ServerModule;
import java.util.logging.Logger;

public class KiwiMain {
    private static final Logger log = Logger.getLogger(KiwiMain.class.getName());

    public static void main(String[] args) throws Exception {
        log.info("Starting initialization Kiwi");
        final long start = System.currentTimeMillis();
        final var observabilityContainer = ObservabilityModule.create();
        final var concurrencyContainer = ConcurrencyModule.create(observabilityContainer);
        final var persistentContainer = PersistentModule.create(observabilityContainer);
        final var serverContainer = ServerModule.create(observabilityContainer, persistentContainer, concurrencyContainer);
        final long end = System.currentTimeMillis();
        log.info("Kiwi initialized in [" + (end - start) + "]ms, starting server");
        serverContainer.start();
    }
}
