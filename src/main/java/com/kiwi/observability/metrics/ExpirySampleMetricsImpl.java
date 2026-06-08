package com.kiwi.observability.metrics;

import com.kiwi.observability.MetricsRegistry;

import static com.kiwi.observability.util.MetricKeys.TTL_ACTIVE_EXPIRED_EVICTIONS;
import static com.kiwi.observability.util.MetricKeys.TTL_SCANNED;

public class ExpirySampleMetricsImpl implements ExpirySampleMetrics {
    private final MetricsRegistry metricsRegistry;

    public ExpirySampleMetricsImpl(MetricsRegistry metricsRegistry) {
        this.metricsRegistry = metricsRegistry;

        this.metricsRegistry.registerCounter(TTL_SCANNED);
        this.metricsRegistry.registerCounter(TTL_ACTIVE_EXPIRED_EVICTIONS);
    }

    @Override
    public void onTtlScanned(int delta) {
        metricsRegistry.updateCounter(TTL_SCANNED, delta);
    }

    @Override
    public void onActiveExpiredEvictions(int delta) {
        metricsRegistry.updateCounter(TTL_ACTIVE_EXPIRED_EVICTIONS, delta);
    }
}
