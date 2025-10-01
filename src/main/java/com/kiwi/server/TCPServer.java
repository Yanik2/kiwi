package com.kiwi.server;

import java.io.IOException;
import java.net.ServerSocket;

public class TCPServer {
    private static final int SOCKET_PORT = 8090;

    private final RequestHandler requestHandler;

    public TCPServer(RequestHandler requestHandler) {
        this.requestHandler = requestHandler;
    }

    //TODO handle exception
    public void start() throws IOException {
        final var serverSocket = new ServerSocket(SOCKET_PORT);

        while (true) {
            final var socket = serverSocket.accept();
            socket.setSoTimeout(30000);

            requestHandler.handle(socket);
        }
    }
}
