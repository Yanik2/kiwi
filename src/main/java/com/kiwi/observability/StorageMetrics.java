package com.kiwi.observability;

import static com.kiwi.observability.util.MetricKeys.STORAGE_TTL_EXPIRED_EVICTION;

public class StorageMetrics {
    private final MetricsRegistry metricsRegistry;

    public StorageMetrics(MetricsRegistry metricsRegistry) {
        this.metricsRegistry = metricsRegistry;

        metricsRegistry.registerCounter(STORAGE_TTL_EXPIRED_EVICTION);
    }

    public void onTtlExpiredEviction() {
        metricsRegistry.updateCounter(STORAGE_TTL_EXPIRED_EVICTION);
    }

}
