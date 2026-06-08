package com.kiwi.server.hook;

import com.kiwi.concurrency.KiwiThreadPoolExecutor;
import com.kiwi.server.accept.TCPServer;
import com.kiwi.server.context.ConnectionRegistry;
import com.kiwi.server.expiration.ExpirySampler;

import java.util.Collection;

public class ShutdownHook {
    private final TCPServer server;
    private final Collection<KiwiThreadPoolExecutor> executors;
    private final ConnectionRegistry connectionRegistry;
    private final ExpirySampler expirySampler;

    public ShutdownHook(TCPServer server,
                        Collection<KiwiThreadPoolExecutor> executor,
                        ConnectionRegistry connectionRegistry,
                        ExpirySampler expirySampler) {
        this.server = server;
        this.executors = executor;
        this.connectionRegistry = connectionRegistry;
        this.expirySampler = expirySampler;
    }

    public void configureShutdown() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            server.stop();
            executors.forEach(KiwiThreadPoolExecutor::shutdown);
            connectionRegistry.shutdown();
            expirySampler.shutdown();
        }));
    }
}
