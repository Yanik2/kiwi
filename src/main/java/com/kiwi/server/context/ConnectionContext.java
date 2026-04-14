package com.kiwi.server.context;

import com.kiwi.server.backpressure.BackPressureGate;
import com.kiwi.server.response.WriterLock;
import com.kiwi.server.response.WriterProxy;
import com.kiwi.server.response.model.TCPResponse;

import java.net.Socket;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

public final class ConnectionContext {
    private static final Logger log = Logger.getLogger(ConnectionContext.class.getName());

    private final UUID connectionId;
    private final Socket socket;
    private final BackPressureGate backPressureGate;
    private volatile boolean isClosed;
    private final AtomicInteger requestIdSequence = new AtomicInteger(1);
    private final WriterProxy writerProxy;
    private final WriterLock writerLock;
    public final ConcurrentHashMap<Integer, Long> times = new ConcurrentHashMap<>();
    public void setTime(int requestId, long time) {
        times.put(requestId, time);
    }

    private volatile int closeAfter = -1;

    public ConnectionContext(UUID connectionId,
                             Socket socket,
                             BackPressureGate backPressureGate,
                             boolean closed,
                             WriterProxy writerProxy,
                             WriterLock writerLock) {
        this.connectionId = connectionId;
        this.socket = socket;
        this.backPressureGate = backPressureGate;
        this.isClosed = closed;
        this.writerProxy = writerProxy;
        this.writerLock = writerLock;
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
            } catch (Exception ex) {
                //ignore if socket already closed
            }
        }
    }

    public int getRequestId() {
        return requestIdSequence.getAndIncrement();
    }

    public void addResponse(TCPResponse tcpResponse) {
        this.backPressureGate.signalIfBelowLow();
        if (!this.isClosed() && writerProxy != null) {
            if (!writerProxy.addResponse(tcpResponse)) {
                log.warning("Trying to add response to context, when writer proxy is not active, " +
                        "or response queue is full. Close connection on slow client. " +
                        "Response id: [" + tcpResponse.requestId() + "], Connection id: [" + connectionId + "]");
                close();
            }
        }

        if (closeAfter == tcpResponse.requestId()) {
            this.isClosed = false;
        }
    }

    public void awaitIfOverload() throws InterruptedException {
        this.backPressureGate.awaitIfOverloaded();
    }

    public void awaitInflight() throws InterruptedException {
        this.writerLock.awaitInflightLevel();
    }

    public void inflightRequest() {
        this.writerLock.onRequest();
    }

    public void closeAfter(int requestId) {
        this.closeAfter = requestId;
        this.writerProxy.setLastResponseId(requestId);
    }

    public void awaitWriterDone() throws InterruptedException {
        writerLock.awaitWriterDone();
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
