package com.kiwi.observability.metrics;

import com.kiwi.observability.MetricsRegistry;

import static com.kiwi.observability.util.MetricKeys.STORAGE_MEMORY_USED_BYTES;

public class StorageNoOpMetrics implements StorageMetrics {
    private final MetricsRegistry metricsRegistry;

    public StorageNoOpMetrics(MetricsRegistry metricsRegistry) {
        this.metricsRegistry = metricsRegistry;
        metricsRegistry.registerGauge(STORAGE_MEMORY_USED_BYTES);
    }


    @Override
    public void onTtlExpiredEviction() {

    }

    @Override
    public void onMemoryBytes(int delta) {
        metricsRegistry.updateGauge(STORAGE_MEMORY_USED_BYTES, delta);
    }

    @Override
    public long getMemoryUsedBytes() {
        // when metrics are disabled it always returns 0
        return metricsRegistry.getGauge(STORAGE_MEMORY_USED_BYTES);
    }

    @Override
    public void onEvictionTriggered() {

    }
}
