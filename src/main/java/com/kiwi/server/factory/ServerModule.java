package com.kiwi.server.factory;

import com.kiwi.observability.ObservabilityModule;
import com.kiwi.processor.DataProcessor;
import com.kiwi.server.RequestDispatcher;
import com.kiwi.server.RequestHandler;
import com.kiwi.server.RequestParser;
import com.kiwi.server.ResponseWriter;
import com.kiwi.server.TCPServer;

public class ServerModule {
    public static TCPServer create(DataProcessor dataProcessor) {
        final var parser = new RequestParser();
        final var observabilityRequestHandler = ObservabilityModule.getRequestHandler();
        final var methodMetrics = ObservabilityModule.getMethodMetrics();
        final var dispatcher = new RequestDispatcher(dataProcessor, observabilityRequestHandler,
            methodMetrics);
        final var responseWriter = new ResponseWriter();
        final var metrics = ObservabilityModule.getRequestMetrics();
        final var handler = new RequestHandler(dispatcher, parser, responseWriter, metrics);
        return new TCPServer(handler);
    }
}
