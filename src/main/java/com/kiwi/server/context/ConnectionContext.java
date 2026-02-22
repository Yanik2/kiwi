package com.kiwi.server.context;

import java.net.Socket;
import java.util.Objects;
import java.util.UUID;

public final class ConnectionContext {
    private final UUID connectionId;
    private final Socket socket;
    private volatile boolean isClosed;

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

    public synchronized boolean isClosed() {
        return isClosed || socket.isClosed();
    }

    public synchronized void close() {
        this.isClosed = true;
        try (socket) {
            socket.setSoLinger(true, 0);
        } catch (Exception ex) {
            //ignore if socket already closed
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (ConnectionContext) obj;
        return Objects.equals(this.connectionId, that.connectionId) &&
                Objects.equals(this.socket, that.socket) &&
                this.isClosed == that.isClosed;
    }

    @Override
    public int hashCode() {
        return Objects.hash(connectionId, socket, isClosed);
    }

    @Override
    public String toString() {
        return "ConnectionContext[" +
                "connectionId=" + connectionId + ", " +
                "socket=" + socket + ", " +
                "closed=" + isClosed + ']';
    }

}
