package com.kiwi.observability.metrics;

import com.kiwi.observability.MetricsRegistry;

import static com.kiwi.observability.util.MetricKeys.STORAGE_TTL_EXPIRED_EVICTION;

public class StorageMetricsImpl implements StorageMetrics {
    private final MetricsRegistry metricsRegistry;

    public StorageMetricsImpl(MetricsRegistry metricsRegistry) {
        this.metricsRegistry = metricsRegistry;

        metricsRegistry.registerCounter(STORAGE_TTL_EXPIRED_EVICTION);
    }

    public void onTtlExpiredEviction() {
        metricsRegistry.updateCounter(STORAGE_TTL_EXPIRED_EVICTION);
    }

}
