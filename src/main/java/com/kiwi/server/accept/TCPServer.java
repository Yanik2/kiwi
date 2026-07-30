package com.kiwi.server.accept;

import com.kiwi.concurrency.KiwiThreadFactory;
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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import static com.kiwi.config.properties.Properties.THREAD_NAME_PREFIX;
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
    private volatile ServerStatus status;
    private ServerSocket serverSocket;
    private final KiwiThreadFactory threadFactory;
    private final AtomicLong threadId = new AtomicLong();
    private final Map<String, Thread> connectionThreadPool = new HashMap<>();

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
        this.threadFactory = new KiwiThreadFactory(THREAD_NAME_PREFIX + "accept-loop-");
    }

    public void start() throws Exception {
        serverSocket = new ServerSocket(socketPort, backlog);
        this.status = RUNNING;

        while (RUNNING == status) {
            Socket socket = null;
            try {
                socket = serverSocket.accept();
                socket.setSoTimeout(soTimeout);

                requestMetrics.onConnection();
                if (requestMetrics.getCurrentClients() > maxClients) {
                    requestMetrics.onRefuse();
                    refuseConnection(socket);
                } else {
                    requestMetrics.onAccept();
                    final var connectionId = threadId.getAndIncrement();
                    final var requestInflightLock = new WriterLock(connectionId);
                    final var writerProxy = new WriterProxy(
                            responseWriter, socket.getOutputStream(), requestMetrics, requestInflightLock, connectionId);
                    final var threadName = THREAD_NAME_PREFIX + "read-loop-" + connectionId;
                    final var connectionContext =
                            new ConnectionContext(
                                    connectionId, socket, backPressureGate, false, writerProxy, requestInflightLock,
                                    () -> connectionThreadPool.remove(threadName)
                            );
                    connectionRegistry.register(connectionContext);
                    final var thread = threadFactory.newThread(
                            () -> connectionReader.readConnection(connectionContext), connectionId);
                    thread.start();
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

        for (Thread t : connectionThreadPool.values()) {
            if (t.isAlive()) {
                t.interrupt();
                t.join(10000);
                if (t.isAlive()) {
                    log.warn("Could not stop thread: " + t.getName() + ". Will be dropped");
                }
            }
        }
        connectionThreadPool.clear();
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
