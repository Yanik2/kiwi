package com.kiwi.observability;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;
import java.util.logging.Level;
import java.util.logging.Logger;

final class MetricsRegistry {
    private static final Logger logger = Logger.getLogger(MetricsRegistry.class.getName());
    private static final MetricsRegistry instance = new MetricsRegistry();

    private final ConnectionCounter connectionCounter = new ConnectionCounter();
    private final BytesCounter bytesCounter = new BytesCounter();
    private final MethodCounter methodCounter = new MethodCounter();

    public static MetricsRegistry getInstance() {
        return instance;
    }

    private MetricsRegistry() {
    }

    public void addAcceptConnection() {
        connectionCounter.onAccepted();
    }

    public void addCloseConnection() {
        connectionCounter.onClosed();
    }

    public void addParsedBytes(long bytesIn) {
        this.bytesCounter.onBytesIn(bytesIn);
    }

    public void addWrittenBytes(long bytesOut) {
        this.bytesCounter.onBytesOut(bytesOut);
    }

    public void addGetRequest() {
        methodCounter.onGet();
    }

    public void addSetRequest() {
        methodCounter.onSet();
    }

    public void addDeleteRequest() {
        methodCounter.onDelete();
    }

    public void addExitRequest() {
        methodCounter.onExit();
    }

    public void addInfoRequest() {
        methodCounter.onInfo();
    }

    public void addUnknownRequest() {
        methodCounter.onUnknown();
    }

    public long getAcceptedConnections() {
        return connectionCounter.connectionsAccepted.sum();
    }

    public long getClosedConnections() {
        return connectionCounter.connectionsClosed.sum();
    }

    public long getCurrentClients() {
        return connectionCounter.clientsCurrent.get();
    }

    public long getBytesIn() {
        return bytesCounter.bytesIn.sum();
    }

    public long getBytesOut() {
        return bytesCounter.bytesOut.sum();
    }

    public long getGetRequests() {
        return methodCounter.getCounter.sum();
    }

    public long getSetRequests() {
        return methodCounter.setCounter.sum();
    }

    public long getDeleteRequests() {
        return methodCounter.deleteCounter.sum();
    }

    public long getExitRequests() {
        return methodCounter.exitCounter.sum();
    }

    public long getInfoRequests() {
        return methodCounter.infoCounter.sum();
    }

    public long getUnknownRequests() {
        return methodCounter.unknownCounter.sum();
    }

    private static class MethodCounter {
        private final LongAdder getCounter = new LongAdder();
        private final LongAdder setCounter = new LongAdder();
        private final LongAdder deleteCounter = new LongAdder();
        private final LongAdder exitCounter = new LongAdder();
        private final LongAdder infoCounter = new LongAdder();
        private final LongAdder unknownCounter = new LongAdder();

        private void onGet() {
            getCounter.increment();
        }

        private void onSet() {
            setCounter.increment();
        }

        private void onDelete() {
            deleteCounter.increment();
        }

        private void onExit() {
            exitCounter.increment();
        }

        private void onInfo() {
            infoCounter.increment();
        }

        private void onUnknown() {
            unknownCounter.increment();
        }
    }

    private static class BytesCounter {
        private final LongAdder bytesIn = new LongAdder();
        private final LongAdder bytesOut = new LongAdder();

        private void onBytesIn(long bytesIn) {
            this.bytesIn.add(bytesIn);
        }

        private void onBytesOut(long bytesOut) {
            this.bytesOut.add(bytesOut);
        }
    }

    private static class ConnectionCounter {
        private final AtomicLong clientsCurrent = new AtomicLong(0);
        private final LongAdder connectionsAccepted = new LongAdder();
        private final LongAdder connectionsClosed = new LongAdder();

        private void onAccepted() {
            connectionsAccepted.increment();
            clientsCurrent.incrementAndGet();
        }

        private void onClosed() {
            connectionsClosed.increment();
            final var currentClients = clientsCurrent.decrementAndGet();
            if (currentClients < 0) {
                logger.log(Level.SEVERE, "Current client is lower then zero: " + currentClients
                    + ", reset to 0");
                clientsCurrent.set(0);
            }
        }
    }
}
