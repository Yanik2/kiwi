package com.kiwi.observability;

public class StorageMetrics {
    private final MetricsRegistry metricsRegistry;

    public StorageMetrics(MetricsRegistry metricsRegistry) {
        this.metricsRegistry = metricsRegistry;
    }

    public void onTtlExpiredEviction() {
        metricsRegistry.addTtlExpiredEviction();
    }

}
