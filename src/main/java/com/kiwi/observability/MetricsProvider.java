package com.kiwi.observability;

import com.kiwi.config.properties.Properties;
import com.kiwi.observability.dto.MetricsDataDto;

public class MetricsProvider {
    public MetricsProvider(MetricsRegistry metricsRegistry) {
        this.metricsRegistry = metricsRegistry;
    }

    private final MetricsRegistry metricsRegistry;

    public MetricsDataDto getMetricsInfo() {
        final var gauges = metricsRegistry.getGauges();
        final var counters = metricsRegistry.getCounters();

        return new MetricsDataDto(
                Properties.PROTOCOL_VERSION,
                Properties.INFO_SCHEMA_VERSION,
                gauges,
                counters,
                metricsRegistry.getServerStart(),
                System.currentTimeMillis() - metricsRegistry.getServerStart()
        );
    }
}
