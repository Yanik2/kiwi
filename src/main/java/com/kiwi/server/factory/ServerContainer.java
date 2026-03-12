package com.kiwi.server.factory;

import com.kiwi.concurrency.KiwiThreadPoolExecutor;
import com.kiwi.server.accept.TCPServer;
import com.kiwi.server.hook.ShutdownHook;

import java.util.Collection;

public record ServerContainer(
        TCPServer server,
        Collection<KiwiThreadPoolExecutor> executors,
        ShutdownHook shutdownHook
) {
    public void start() throws Exception {
        shutdownHook.configureShutdown();
        executors.forEach(KiwiThreadPoolExecutor::start);
        server.start();
    }
}
