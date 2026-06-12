package com.kiwi.observability.metrics;

import com.kiwi.observability.MetricsRegistry;

import static com.kiwi.observability.util.MetricKeys.STORAGE_EVICTION_TRIGGERED;
import static com.kiwi.observability.util.MetricKeys.STORAGE_KEYS_WITH_EXPIRATION;
import static com.kiwi.observability.util.MetricKeys.STORAGE_MEMORY_USED_BYTES;
import static com.kiwi.observability.util.MetricKeys.STORAGE_TTL_EXPIRED_EVICTION;

public class StorageMetricsImpl implements StorageMetrics {
    private final MetricsRegistry metricsRegistry;

    public StorageMetricsImpl(MetricsRegistry metricsRegistry) {
        this.metricsRegistry = metricsRegistry;

        metricsRegistry.registerCounter(STORAGE_TTL_EXPIRED_EVICTION);
        metricsRegistry.registerGauge(STORAGE_MEMORY_USED_BYTES);
        metricsRegistry.registerCounter(STORAGE_EVICTION_TRIGGERED);
        metricsRegistry.registerGauge(STORAGE_KEYS_WITH_EXPIRATION);
    }

    public void onTtlExpiredEviction() {
        metricsRegistry.updateCounter(STORAGE_TTL_EXPIRED_EVICTION);
    }

    @Override
    public void onMemoryBytes(int delta) {
        metricsRegistry.updateGauge(STORAGE_MEMORY_USED_BYTES, delta);
    }

    @Override
    public long getMemoryUsedBytes() {
        return metricsRegistry.getGauge(STORAGE_MEMORY_USED_BYTES);
    }

    @Override
    public void onEvictionTriggered() {
        metricsRegistry.updateCounter(STORAGE_EVICTION_TRIGGERED);
    }

    @Override
    public void onKeyWithExpiration(int delta) {
        metricsRegistry.updateGauge(STORAGE_KEYS_WITH_EXPIRATION, delta);
    }

}
