package com.kiwi.server.factory;

import com.kiwi.concurrency.ConcurrencyModule;
import com.kiwi.observability.ObservabilityModule;
import com.kiwi.persistent.Storage;
import com.kiwi.server.ShutdownHook;
import com.kiwi.server.backpressure.BackPressureGate;
import com.kiwi.server.context.ConnectionRegistry;
import com.kiwi.server.request.ConnectionReader;
import com.kiwi.server.request.RequestHandler;
import com.kiwi.server.dispatcher.command.RequestDispatcher;
import com.kiwi.server.validator.BaseRequestValidator;
import com.kiwi.server.response.ResponseWriter;
import com.kiwi.server.accept.TCPServer;
import com.kiwi.server.parsing.BinaryRequestParser;

public class ServerModule {
    private static final String THREAD_POOL_EXECUTOR_NAME = "server-executor";
    private static final String THREAD_POOL_NAME = "server-thread-pool";
    // TO CONFIG IN PHASE 5
    private static final int THREAD_POOL_SIZE = 9;
    private static final int THREAD_POOL_QUEUE_CAP = 1000;

    public static TCPServer create(Storage storage) {
        final var parser = new BinaryRequestParser();
        final var observabilityRequestHandler = ObservabilityModule.getRequestHandler();
        final var methodMetrics = ObservabilityModule.getMethodMetrics();
        final var dispatcher = RequestDispatcher.create(observabilityRequestHandler,
            methodMetrics, storage);
        final var responseWriter = new ResponseWriter();
        final var metrics = ObservabilityModule.getRequestMetrics();
        final var requestValidator = new BaseRequestValidator();
        final var requestHandler = new RequestHandler(dispatcher, requestValidator, metrics);
        final var tpMetrics = ObservabilityModule.getThreadPoolMetrics(THREAD_POOL_NAME);
        final var threadPoolExecutor = ConcurrencyModule.createExecutor(
                THREAD_POOL_EXECUTOR_NAME, THREAD_POOL_NAME, THREAD_POOL_SIZE, THREAD_POOL_QUEUE_CAP, tpMetrics);
        threadPoolExecutor.start();
        final var connectionRegistry = new ConnectionRegistry();
        final var connectionReader =
                new ConnectionReader(parser, metrics, threadPoolExecutor, requestHandler, connectionRegistry);
        final var server = new TCPServer(connectionReader, responseWriter, metrics,
                new BackPressureGate(threadPoolExecutor, tpMetrics), connectionRegistry);
        final var shutdownHook = new ShutdownHook(server, threadPoolExecutor, connectionRegistry);
        shutdownHook.configureShutdown();
        return server;
    }
}
