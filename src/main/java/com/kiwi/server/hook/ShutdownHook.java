package com.kiwi.server.hook;

import com.kiwi.concurrency.KiwiThreadPoolExecutor;
import com.kiwi.jvm.jfr.JfrController;
import com.kiwi.server.accept.TCPServer;
import com.kiwi.server.context.ConnectionRegistry;
import com.kiwi.server.expiration.ExpirySampler;

import java.util.Collection;

public class ShutdownHook {
    private final TCPServer server;
    private final Collection<KiwiThreadPoolExecutor> executors;
    private final ConnectionRegistry connectionRegistry;
    private final ExpirySampler expirySampler;
    private final JfrController jfrController;

    public ShutdownHook(TCPServer server,
                        Collection<KiwiThreadPoolExecutor> executor,
                        ConnectionRegistry connectionRegistry,
                        ExpirySampler expirySampler,
                        JfrController jfrController) {
        this.server = server;
        this.executors = executor;
        this.connectionRegistry = connectionRegistry;
        this.expirySampler = expirySampler;
        this.jfrController = jfrController;
    }

    public void configureShutdown() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            server.stop();
            executors.forEach(KiwiThreadPoolExecutor::shutdown);
            connectionRegistry.shutdown();
            expirySampler.shutdown();
            jfrController.stop();
        }));
    }
}
