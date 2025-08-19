package com.kiwi.server.factory;

import com.kiwi.processor.DataProcessor;
import com.kiwi.server.RequestDispatcher;
import com.kiwi.server.RequestHandler;
import com.kiwi.server.RequestParser;
import com.kiwi.server.ResponseWriter;
import com.kiwi.server.TCPServer;

public class ServerModule {
    public static TCPServer create(DataProcessor dataProcessor) {
        final var parser = new RequestParser();
        final var dispatcher = new RequestDispatcher(dataProcessor);
        final var responseWriter = new ResponseWriter();
        final var handler = new RequestHandler(dispatcher, parser, responseWriter);
        return new TCPServer(handler);
    }
}
