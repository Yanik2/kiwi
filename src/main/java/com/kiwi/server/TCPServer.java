package com.kiwi.server;

import com.kiwi.observability.RequestMetrics;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TCPServer {
    private static final Logger log = Logger.getLogger(TCPServer.class.getName());

    //TODO will be moved to properties in phase 5
    private static final int SOCKET_PORT = 8090;
    private static final int MAX_CLIENTS = 1000;

    private final RequestReader requestReader;
    private final RequestMetrics requestMetrics;

    public TCPServer(RequestReader requestReader, RequestMetrics requestMetrics) {
        this.requestReader = requestReader;
        this.requestMetrics = requestMetrics;
    }

    //TODO handle exception
    public void start() throws IOException {
        final var serverSocket = new ServerSocket(SOCKET_PORT);

        while (true) {
            final var socket = serverSocket.accept();
            //for testing purposes timeout for 10 min
            socket.setSoTimeout(600000);

            if (requestMetrics.getCurrentClients() >= MAX_CLIENTS) {
                requestMetrics.onRefuse();
                refuseConnection(socket);
            } else {
                requestMetrics.onAccept();
                requestReader.readRequest(socket);
            }
        }

    }

    private void refuseConnection(Socket socket) {
        log.info("Maximum clients exceeded, refusing connection. Max clients: " + MAX_CLIENTS + ". Current clients: "
                + requestMetrics.getCurrentClients());
        try {
            socket.setSoLinger(true, 0);
            socket.close();
        } catch (Exception ex) {
            log.log(Level.WARNING, "Unexpected error on socket closing");
        }
    }
}
