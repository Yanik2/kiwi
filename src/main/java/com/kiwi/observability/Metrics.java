package com.kiwi.observability;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class Metrics {
    private static final Logger logger = Logger.getLogger(Metrics.class.getName());
    private static final Metrics instance = new Metrics();

    private final AtomicLong clientsCurrent = new AtomicLong(0);
    private final LongAdder connectionsAccepted = new LongAdder();
    private final LongAdder connectionsClosed = new LongAdder();

    public static Metrics getInstance() {
        return instance;
    }

    private Metrics() {
    }

    public void addConnectionAccepted() {
        connectionsAccepted.increment();
        clientsCurrent.incrementAndGet();
    }

    public void addConnectionsClosed() {
        connectionsClosed.increment();
        final var currentClients = clientsCurrent.decrementAndGet();
        if (currentClients < 0) {
            logger.log(Level.SEVERE, "Current client is lower then zero: " + currentClients
                + ", reset to 0");
            clientsCurrent.set(0);
        }
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
}
