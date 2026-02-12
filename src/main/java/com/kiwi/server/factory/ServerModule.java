package com.kiwi.server.factory;

import com.kiwi.observability.ObservabilityModule;
import com.kiwi.persistent.Storage;
import com.kiwi.server.BinaryRequestParser;
import com.kiwi.server.dispatcher.command.RequestDispatcher;
import com.kiwi.server.RequestHandler;
import com.kiwi.server.ResponseWriter;
import com.kiwi.server.TCPServer;

public class ServerModule {
    public static TCPServer create(Storage storage) {
        final var parser = new BinaryRequestParser();
        final var observabilityRequestHandler = ObservabilityModule.getRequestHandler();
        final var methodMetrics = ObservabilityModule.getMethodMetrics();
        final var dispatcher = RequestDispatcher.create(observabilityRequestHandler,
            methodMetrics, storage);
        final var responseWriter = new ResponseWriter();
        final var metrics = ObservabilityModule.getRequestMetrics();
        final var handler = RequestHandler.create(dispatcher, parser, responseWriter, metrics);
        return new TCPServer(handler);
    }
}
