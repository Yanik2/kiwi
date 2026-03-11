package com.kiwi.server.hook;

import com.kiwi.concurrency.KiwiThreadPoolExecutor;
import com.kiwi.server.accept.TCPServer;
import com.kiwi.server.context.ConnectionRegistry;

public class ShutdownHook {
    private final TCPServer server;
    private final KiwiThreadPoolExecutor executor;
    private final ConnectionRegistry connectionRegistry;

    public ShutdownHook(TCPServer server, KiwiThreadPoolExecutor executor, ConnectionRegistry connectionRegistry) {
        this.server = server;
        this.executor = executor;
        this.connectionRegistry = connectionRegistry;
    }

    public void configureShutdown() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            server.stop();
            executor.shutdown();
            connectionRegistry.shutdown();
        }));
    }
}
