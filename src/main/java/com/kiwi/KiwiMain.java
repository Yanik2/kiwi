package com.kiwi;

import com.kiwi.concurrency.factory.ConcurrencyModule;
import com.kiwi.config.ConfigModule;
import com.kiwi.log.KiwiLogger;
import com.kiwi.log.KiwiLoggerFactory;
import com.kiwi.observability.factory.ObservabilityModule;
import com.kiwi.persistent.factory.PersistentModule;
import com.kiwi.server.factory.ServerModule;

public class KiwiMain {
    private static final KiwiLogger log = KiwiLoggerFactory.getLogger(KiwiMain.class.getName());

    public static void main(String[] args) throws Exception {
        log.info("Starting initialization Kiwi");
        final long start = System.currentTimeMillis();
        final var configContainer = ConfigModule.createConfig();
        final var observabilityContainer = ObservabilityModule.create(configContainer);
        final var concurrencyContainer = ConcurrencyModule.create(observabilityContainer);
        final var persistentContainer = PersistentModule.create(observabilityContainer);
        final var serverContainer = ServerModule.create(observabilityContainer, persistentContainer,
                concurrencyContainer, configContainer);
        final long end = System.currentTimeMillis();
        log.info("Kiwi initialized in [" + (end - start) + "]ms, starting server");
        serverContainer.start();
    }
}
