package com.kiwi.concurrency.task;

import com.kiwi.server.RequestHandler;

import java.net.Socket;
import java.util.UUID;

public class RequestTask implements Task {
    //TODO do i need task id? for now no, but later will be used
    private final UUID taskId;
    private final RequestHandler requestHandler;
    private final Socket socket;
    // timeout time for task in seconds
    private final int timeout;

    public RequestTask(RequestHandler requestHandler, Socket socket, int timeout) {
        this.taskId = UUID.randomUUID();
        this.requestHandler = requestHandler;
        this.socket = socket;
        this.timeout = timeout;
    }

    public void execute() {
        requestHandler.handle(socket);
    }

    public void reject() {
        requestHandler.refuse(socket);
    }

    public int getTimeout() {
        return this.timeout;
    }
}
