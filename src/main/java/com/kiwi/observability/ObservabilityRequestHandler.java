package com.kiwi.observability;

import com.kiwi.observability.dto.MetricsDataDto;

public class ObservabilityRequestHandler {
    public ObservabilityRequestHandler(Metrics metrics) {
        this.metrics = metrics;
    }

    private final Metrics metrics;

    public MetricsDataDto getMetricsInfo() {
        return new MetricsDataDto(metrics.getAcceptedConnections(),
            metrics.getClosedConnections(), metrics.getCurrentClients());
    }
}
