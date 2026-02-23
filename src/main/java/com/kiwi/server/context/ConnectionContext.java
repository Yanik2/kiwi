package com.kiwi.server.context;

import java.net.Socket;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public final class ConnectionContext {
    private final UUID connectionId;
    private final Socket socket;
    private volatile boolean isClosed;
    private final AtomicInteger requestIdSequence = new AtomicInteger();

    public ConnectionContext(UUID connectionId, Socket socket, boolean closed) {
        this.connectionId = connectionId;
        this.socket = socket;
        this.isClosed = closed;
    }

    public UUID connectionId() {
        return connectionId;
    }

    public Socket socket() {
        return socket;
    }

    public boolean isClosed() {
        return isClosed || socket.isClosed();
    }

    public synchronized void close() {
        if (!isClosed) {
            this.isClosed = true;
            try (socket) {
                socket.setSoLinger(true, 0);
            } catch (Exception ex) {
                //ignore if socket already closed
            }
        }
    }

    public int getRequestId() {
        return requestIdSequence.incrementAndGet();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (ConnectionContext) obj;
        return Objects.equals(this.connectionId, that.connectionId) &&
                this.isClosed == that.isClosed;
    }

    @Override
    public int hashCode() {
        return Objects.hash(connectionId, isClosed);
    }

    @Override
    public String toString() {
        return "ConnectionContext[" +
                "connectionId=" + connectionId + ", " +
                "socket=" + socket + ", " +
                "closed=" + isClosed + ']';
    }

}
