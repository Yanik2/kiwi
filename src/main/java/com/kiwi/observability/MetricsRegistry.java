package com.kiwi.observability;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;
import java.util.logging.Level;
import java.util.logging.Logger;

final class MetricsRegistry {
    private static final Logger logger = Logger.getLogger(MetricsRegistry.class.getName());
    private static final MetricsRegistry instance = new MetricsRegistry();

    private final AtomicLong clientsCurrent = new AtomicLong(0);
    private final LongAdder connectionsAccepted = new LongAdder();
    private final LongAdder connectionsClosed = new LongAdder();
    private final LongAdder bytesIn = new LongAdder();
    private final LongAdder bytesOut = new LongAdder();

    public static MetricsRegistry getInstance() {
        return instance;
    }

    private MetricsRegistry() {
    }

    public void addAcceptConnection() {
        connectionsAccepted.increment();
        clientsCurrent.incrementAndGet();
    }

    public void addCloseConnection() {
        connectionsClosed.increment();
        final var currentClients = clientsCurrent.decrementAndGet();
        if (currentClients < 0) {
            logger.log(Level.SEVERE, "Current client is lower then zero: " + currentClients
                + ", reset to 0");
            clientsCurrent.set(0);
        }
    }

    public void addParsedBytes(long bytesIn) {
        this.bytesIn.add(bytesIn);
    }

    public void addWrittenBytes(long bytesOut) {
        this.bytesOut.add(bytesOut);
    }

    public long getAcceptedConnections() {
        return connectionsAccepted.sum();
    }

    public long getClosedConnections() {
        return connectionsClosed.sum();
    }

    public long getCurrentClients() {
        return clientsCurrent.get();
    }

    public long getBytesIn() {
        return bytesIn.sum();
    }

    public long getBytesOut() {
        return bytesOut.sum();
    }
}
