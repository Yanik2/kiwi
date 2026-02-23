package com.kiwi.concurrency.task;

import com.kiwi.server.RequestHandler;
import com.kiwi.server.context.ConnectionContext;
import com.kiwi.server.dto.TCPRequest;

public class ConnectionTask implements Task {
    private final RequestHandler requestHandler;
    private final ConnectionContext connectionContext;
    private final TCPRequest request;
    private final int timeout;

    public ConnectionTask(RequestHandler requestHandler,
                          ConnectionContext connectionContext,
                          TCPRequest request,
                          int timeout) {
        this.requestHandler = requestHandler;
        this.connectionContext = connectionContext;
        this.request = request;
        this.timeout = timeout;
    }

    @Override
    public void execute() {
        requestHandler.handle(request, connectionContext);
    }

    @Override
    public void reject() {
        requestHandler.reject(request, connectionContext);
    }

    @Override
    public int getTimeout() {
        return this.timeout;
    }
}
