package com.kiwi.observability;

import com.kiwi.observability.dto.MetricsDataDto;

public class ObservabilityRequestHandler {
    public ObservabilityRequestHandler(MetricsRegistry metricsRegistry) {
        this.metricsRegistry = metricsRegistry;
    }

    private final MetricsRegistry metricsRegistry;

    public MetricsDataDto getMetricsInfo() {
        return new MetricsDataDto(
            metricsRegistry.getAcceptedConnections(),
            metricsRegistry.getClosedConnections(),
            metricsRegistry.getCurrentClients(),
            metricsRegistry.getBytesIn(),
            metricsRegistry.getBytesOut()
        );
    }
}
