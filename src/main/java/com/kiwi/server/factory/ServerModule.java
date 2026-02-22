package com.kiwi.server.factory;

import com.kiwi.concurrency.ConcurrencyModule;
import com.kiwi.observability.ObservabilityModule;
import com.kiwi.persistent.Storage;
import com.kiwi.server.ConnectionReader;
import com.kiwi.server.RequestHandler;
import com.kiwi.server.context.ConnectionRegistry;
import com.kiwi.server.dispatcher.command.RequestDispatcher;
import com.kiwi.server.validator.BaseRequestValidator;
import com.kiwi.server.ResponseWriter;
import com.kiwi.server.TCPServer;
import com.kiwi.server.parsing.BinaryRequestParser;

public class ServerModule {
    private static final String THREAD_POOL_EXECUTOR_NAME = "server-executor";
    private static final String THREAD_POOL_NAME = "server-thread-pool";

    public static TCPServer create(Storage storage) {
        final var parser = new BinaryRequestParser();
        final var observabilityRequestHandler = ObservabilityModule.getRequestHandler();
        final var methodMetrics = ObservabilityModule.getMethodMetrics();
        final var dispatcher = RequestDispatcher.create(observabilityRequestHandler,
            methodMetrics, storage);
        final var responseWriter = new ResponseWriter();
        final var metrics = ObservabilityModule.getRequestMetrics();
        final var requestValidator = new BaseRequestValidator();
        final var requestHandler = new RequestHandler(dispatcher, responseWriter, requestValidator, metrics);
        final var threadPoolExecutor = ConcurrencyModule.createExecutor(
                THREAD_POOL_EXECUTOR_NAME, THREAD_POOL_NAME, 100, 100);
        threadPoolExecutor.start();
        final var connectionReader =
                new ConnectionReader(parser, metrics, responseWriter, threadPoolExecutor, requestHandler);
        return new TCPServer(connectionReader, metrics, new ConnectionRegistry());
    }
}
