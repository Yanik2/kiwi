package com.kiwi.observability;

public final class RequestMetrics {
    private final MetricsRegistry metricsRegistry;

    public RequestMetrics(MetricsRegistry metricsRegistry) {
        this.metricsRegistry = metricsRegistry;
    }

    public void onAccept() {
        metricsRegistry.addAcceptConnection();
    }

    public void onClose() {
        metricsRegistry.addCloseConnection();
    }

    public void onParse(long bytes) {
        metricsRegistry.addParsedBytes(bytes);
    }

    public void onWrite(long bytes) {
        metricsRegistry.addWrittenBytes(bytes);
    }
}
