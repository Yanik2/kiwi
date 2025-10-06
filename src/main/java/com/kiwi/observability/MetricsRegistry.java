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
    private final ProtoErrorCounter protoErrorCounter = new ProtoErrorCounter();
    private final ServerCounters serverCounters = new ServerCounters();

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

    public void addRefusedConnection() {
        connectionCounter.onRefused();
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

    public void addPingRequest() {
        methodCounter.onPing();
    }

    public void addUnknownMethodError() {
        protoErrorCounter.onUnknownMethod();
    }

    public void addHeaderTooLongError() {
        protoErrorCounter.onHeaderTooLong();
    }

    public void addValueTooLongError() {
        protoErrorCounter.onValueTooLong();
    }

    public void addKeyTooLongError() {
        protoErrorCounter.onKeyTooLong();
    }

    public void addUnexpectedEndOfFileError() {
        protoErrorCounter.onUnexpectedEndOfFile();
    }

    public void addNonDigitInLengthError() {
        protoErrorCounter.onNonDigitInLength();
    }

    public void addInvalidSeparatorError() {
        protoErrorCounter.onInvalidSeparator();
    }

    public void addMethodTooLongError() {
        protoErrorCounter.onMethodTooLong();
    }

    // getters

    public long getAcceptedConnections() {
        return connectionCounter.connectionsAccepted.sum();
    }

    public long getClosedConnections() {
        return connectionCounter.connectionsClosed.sum();
    }

    public long getRefusedConnections() {
        return connectionCounter.connectionRefused.sum();
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

    public long getPingRequests() {
        return methodCounter.pingCounter.sum();
    }

    public long getUnknownMethods() {
        return protoErrorCounter.unknownMethodCounter.sum();
    }

    public long getHeaderTooLong() {
        return protoErrorCounter.headerTooLongCounter.sum();
    }

    public long getValueTooLong() {
        return protoErrorCounter.valueTooLongCounter.sum();
    }

    public long getKeyTooLong() {
        return protoErrorCounter.keyTooLongCounter.sum();
    }

    public long getUnexpectedEndOfFile() {
        return protoErrorCounter.unexpectedEndOfFileCounter.sum();
    }

    public long getNonDigitInLength() {
        return protoErrorCounter.nonDigitInLengthCounter.sum();
    }

    public long getInvalidSeparator() {
        return protoErrorCounter.invalidSeparatorCounter.sum();
    }

    public long getServerStart() {
        return serverCounters.startUpMillis;
    }

    private static class MethodCounter {
        private final LongAdder getCounter = new LongAdder();
        private final LongAdder setCounter = new LongAdder();
        private final LongAdder deleteCounter = new LongAdder();
        private final LongAdder exitCounter = new LongAdder();
        private final LongAdder infoCounter = new LongAdder();
        private final LongAdder pingCounter = new LongAdder();

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

        private void onPing() {
            pingCounter.increment();
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
        private final LongAdder connectionRefused = new LongAdder();

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

        private void onRefused() {
            connectionRefused.increment();
        }
    }

    private static class ProtoErrorCounter {
        private final LongAdder unknownMethodCounter = new LongAdder();
        private final LongAdder headerTooLongCounter = new LongAdder();
        private final LongAdder valueTooLongCounter = new LongAdder();
        private final LongAdder keyTooLongCounter = new LongAdder();
        private final LongAdder unexpectedEndOfFileCounter = new LongAdder();
        private final LongAdder nonDigitInLengthCounter = new LongAdder();
        private final LongAdder invalidSeparatorCounter = new LongAdder();
        private final LongAdder methodTooLongCounter = new LongAdder();

        private void onUnknownMethod() {
            unknownMethodCounter.increment();
        }

        private void onHeaderTooLong() {
            headerTooLongCounter.increment();
        }

        private void onValueTooLong() {
            valueTooLongCounter.increment();
        }

        private void onKeyTooLong() {
            keyTooLongCounter.increment();
        }

        private void onUnexpectedEndOfFile() {
            unexpectedEndOfFileCounter.increment();
        }

        private void onNonDigitInLength() {
            nonDigitInLengthCounter.increment();
        }

        private void onInvalidSeparator() {
            invalidSeparatorCounter.increment();
        }

        public void onMethodTooLong() {
            methodTooLongCounter.increment();
        }
    }

    private static class ServerCounters {
        private final long startUpMillis = System.currentTimeMillis();
    }
}
