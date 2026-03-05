package com.kiwi.server;

import com.kiwi.observability.RequestMetrics;
import com.kiwi.server.backpressure.BackPressureGate;
import com.kiwi.server.context.ConnectionContext;
import com.kiwi.server.request.ConnectionReader;
import com.kiwi.server.response.ResponseWriter;
import com.kiwi.server.response.WriterProxy;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TCPServer {
    private static final Logger log = Logger.getLogger(TCPServer.class.getName());

    //TODO will be moved to properties in phase 5
    private static final int SOCKET_PORT = 8090;
    private static final int MAX_CLIENTS = 1000;

    private final ConnectionReader connectionReader;
    private final ResponseWriter responseWriter;
    private final RequestMetrics requestMetrics;
    private final BackPressureGate backPressureGate;

    private final ExecutorService connectionThreadPool = Executors.newCachedThreadPool();

    public TCPServer(ConnectionReader connectionReader, ResponseWriter responseWriter,
                     RequestMetrics requestMetrics, BackPressureGate backPressureGate) {
        this.connectionReader = connectionReader;
        this.responseWriter = responseWriter;
        this.requestMetrics = requestMetrics;
        this.backPressureGate = backPressureGate;
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
                final var writerProxy = new WriterProxy(responseWriter, socket.getOutputStream(), requestMetrics);
                final var connectionContext =
                        new ConnectionContext(UUID.randomUUID(), socket, backPressureGate, false, writerProxy);
                connectionThreadPool.execute(() -> connectionReader.readConnection(connectionContext));
            }
        }

        // thread pool shutdown will be implemented on graceful shutdown implementation

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
