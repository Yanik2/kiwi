package com.kiwi.server.context;

import com.kiwi.server.response.WriterProxy;
import com.kiwi.server.response.model.TCPResponse;

import java.net.Socket;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

public final class ConnectionContext {
    private static final Logger log = Logger.getLogger(ConnectionContext.class.getName());

    private final UUID connectionId;
    private final Socket socket;
    private volatile boolean isClosed;
    private final AtomicInteger requestIdSequence = new AtomicInteger();

    private WriterProxy writerProxy;

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
                writerProxy.stop(!socket.isClosed());
                while (!writerProxy.isDrained()) {}
                socket.setSoLinger(true, 0);
            } catch (Exception ex) {
                //ignore if socket already closed
            }
        }
    }

    public int getRequestId() {
        return requestIdSequence.incrementAndGet();
    }

    public int getRecentRequestId() {
        return requestIdSequence.get();
    }

    public void setWriterProxy(WriterProxy writerProxy) {
        this.writerProxy = writerProxy;
    }

    public void addResponse(TCPResponse tcpResponse) {
        if (!this.isClosed() && writerProxy != null) {
            if (!writerProxy.addResponse(tcpResponse)) {
                log.warning("Trying to add response to context, when writer proxy is not active, " +
                        "or response queue is full. Response id: [" + tcpResponse.requestId() + "]");
            }
        }
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
