package com.kiwi.server.accept;

import com.kiwi.log.KiwiLogger;
import com.kiwi.log.KiwiLoggerFactory;
import com.kiwi.observability.metrics.RequestMetrics;
import com.kiwi.server.backpressure.BackPressureGate;
import com.kiwi.server.context.ConnectionContext;
import com.kiwi.server.context.ConnectionRegistry;
import com.kiwi.server.request.ConnectionReader;
import com.kiwi.server.response.WriterLock;
import com.kiwi.server.response.ResponseWriter;
import com.kiwi.server.response.WriterProxy;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.kiwi.server.accept.ServerStatus.RUNNING;
import static com.kiwi.server.accept.ServerStatus.STOPPED;
import static com.kiwi.server.accept.ServerStatus.STOPPING;
import static java.util.concurrent.TimeUnit.SECONDS;

public class TCPServer {
    private static final KiwiLogger log = KiwiLoggerFactory.getLogger(TCPServer.class.getName());

    private final ConnectionReader connectionReader;
    private final ResponseWriter responseWriter;
    private final RequestMetrics requestMetrics;
    private final BackPressureGate backPressureGate;
    private final ConnectionRegistry connectionRegistry;
    private final int socketPort;
    private final int soTimeout;
    private final int maxClients;
    private final int backlog;

    private final ExecutorService connectionThreadPool = Executors.newCachedThreadPool();

    private volatile ServerStatus status;

    private ServerSocket serverSocket;

    public TCPServer(ConnectionReader connectionReader, ResponseWriter responseWriter,
                     RequestMetrics requestMetrics, BackPressureGate backPressureGate,
                     ConnectionRegistry connectionRegistry, int socketPort, int soTimeout, int maxClients, int backlog) {
        this.connectionReader = connectionReader;
        this.responseWriter = responseWriter;
        this.requestMetrics = requestMetrics;
        this.backPressureGate = backPressureGate;
        this.connectionRegistry = connectionRegistry;
        this.socketPort = socketPort;
        this.soTimeout = soTimeout;
        this.maxClients = maxClients;
        this.backlog = backlog;
    }

    public void start() throws Exception {
        serverSocket = new ServerSocket(socketPort, backlog);
        this.status = RUNNING;

        while (RUNNING == status) {
            Socket socket = null;
            try {
                socket = serverSocket.accept();

                //for testing purposes timeout for 10 min
//                socket.setSoTimeout(soTimeout);
                socket.setSoTimeout(600000);

                requestMetrics.onConnection();
                if (requestMetrics.getCurrentClients() > maxClients) {
                    requestMetrics.onRefuse();
                    refuseConnection(socket);
                } else {
                    requestMetrics.onAccept();
                    final var requestInflightLock = new WriterLock();
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
                    log.error("Server socket is closed with exception", ex.getMessage());
                    break;
                } else if (socket != null && socket.isClosed()) {
                    log.warn("Connection socket is closed, continue accept connections", ex.getMessage());
                }
                else {
                    log.warn("Server socket exception, continue accepting", ex.getMessage());
                }
            }

        }

        connectionThreadPool.shutdown();
        if (!connectionThreadPool.awaitTermination(10, SECONDS)) {
            log.warn("Server will be stopped immediately", "Timeout elapsed on readers threads stop");
            connectionThreadPool.shutdownNow();
        }
        this.status = STOPPED;
    }

    public void stop() {
        this.status = STOPPING;
        try {
            this.serverSocket.close();
        } catch (IOException ex) {
            log.warn("Exception on closing server socket", ex.getMessage());
        }
    }

    private void refuseConnection(Socket socket) {
        log.info("Maximum clients exceeded, refusing connection. Max clients: " + maxClients + ". Current clients: "
                + requestMetrics.getCurrentClients());
        try {
            socket.setSoLinger(true, 0);
            socket.close();
        } catch (Exception ex) {
            log.warn("Unexpected error on socket closing", ex.getMessage());
        }
    }
}
