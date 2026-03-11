package com.kiwi.server.accept;

import com.kiwi.observability.RequestMetrics;
import com.kiwi.server.backpressure.BackPressureGate;
import com.kiwi.server.context.ConnectionContext;
import com.kiwi.server.context.ConnectionRegistry;
import com.kiwi.server.request.ConnectionReader;
import com.kiwi.server.request.RequestInflightLock;
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

import static com.kiwi.server.accept.ServerStatus.RUNNING;
import static com.kiwi.server.accept.ServerStatus.STOPPED;
import static com.kiwi.server.accept.ServerStatus.STOPPING;
import static java.util.concurrent.TimeUnit.SECONDS;

public class TCPServer {
    private static final Logger log = Logger.getLogger(TCPServer.class.getName());

    //TODO will be moved to properties in phase 5
    private static final int SOCKET_PORT = 8090;
    private static final int MAX_CLIENTS = 1000;

    private final ConnectionReader connectionReader;
    private final ResponseWriter responseWriter;
    private final RequestMetrics requestMetrics;
    private final BackPressureGate backPressureGate;
    private final ConnectionRegistry connectionRegistry;

    private final ExecutorService connectionThreadPool = Executors.newCachedThreadPool();

    private volatile ServerStatus status;

    private ServerSocket serverSocket;

    public TCPServer(ConnectionReader connectionReader, ResponseWriter responseWriter,
                     RequestMetrics requestMetrics, BackPressureGate backPressureGate,
                     ConnectionRegistry connectionRegistry) {
        this.connectionReader = connectionReader;
        this.responseWriter = responseWriter;
        this.requestMetrics = requestMetrics;
        this.backPressureGate = backPressureGate;
        this.connectionRegistry = connectionRegistry;
    }

    public void start() throws Exception {
        serverSocket = new ServerSocket(SOCKET_PORT);
        this.status = RUNNING;

        while (RUNNING == status) {
            Socket socket = null;
            try {
                socket = serverSocket.accept();
                requestMetrics.onConnection();

                //for testing purposes timeout for 10 min
                socket.setSoTimeout(600000);

                if (requestMetrics.getCurrentClients() >= MAX_CLIENTS) {
                    requestMetrics.onRefuse();
                    refuseConnection(socket);
                } else {
                    requestMetrics.onAccept();
                    final var requestInflightLock = new RequestInflightLock();
                    final var writerProxy = new WriterProxy(
                            responseWriter, socket.getOutputStream(), requestMetrics, requestInflightLock);
                    final var connectionContext =
                            new ConnectionContext(
                                    UUID.randomUUID(), socket, backPressureGate, false, writerProxy, requestInflightLock
                            );
                    connectionRegistry.register(connectionContext);
                    connectionThreadPool.execute(() -> connectionReader.readConnection(connectionContext));
                }
            } catch (Exception ex) {
                if (STOPPING == status) {
                    break;
                } else if (serverSocket.isClosed()) {
                    this.status = STOPPING;
                    log.severe("Server socket is closed with exception: " + ex.getMessage());
                    break;
                } else if (socket != null && socket.isClosed()) {
                    log.warning("Connection socket is closed, continue accept connections. Exception: "
                            + ex.getMessage());
                }
                else {
                    log.warning("Server socket exception: " + ex.getMessage() + ", continue accepting");
                }
            }

        }

        connectionThreadPool.shutdown();
        if (!connectionThreadPool.awaitTermination(10, SECONDS)) {
            log.warning("Timeout elapsed on readers threads stop, will be stopped immediately");
            connectionThreadPool.shutdownNow();
        }
        this.status = STOPPED;
    }

    public void stop() {
        this.status = STOPPING;

        try {
            this.serverSocket.close();
        } catch (IOException ex) {
            log.warning("Exception on closing server socket: " + ex.getMessage());
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
