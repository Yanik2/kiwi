package com.kiwi.server.hook;

import com.kiwi.concurrency.KiwiThreadPoolExecutor;
import com.kiwi.server.accept.TCPServer;
import com.kiwi.server.context.ConnectionRegistry;

import java.util.Collection;

public class ShutdownHook {
    private final TCPServer server;
    private final Collection<KiwiThreadPoolExecutor> executors;
    private final ConnectionRegistry connectionRegistry;

    public ShutdownHook(TCPServer server,
                        Collection<KiwiThreadPoolExecutor> executor,
                        ConnectionRegistry connectionRegistry) {
        this.server = server;
        this.executors = executor;
        this.connectionRegistry = connectionRegistry;
    }

    public void configureShutdown() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            server.stop();
            executors.forEach(KiwiThreadPoolExecutor::shutdown);
            connectionRegistry.shutdown();
        }));
    }
}
