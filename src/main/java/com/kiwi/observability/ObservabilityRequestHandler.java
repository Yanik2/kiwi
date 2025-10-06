package com.kiwi.observability;

import com.kiwi.observability.dto.MetricsDataDto;

public class ObservabilityRequestHandler {
    ObservabilityRequestHandler(MetricsRegistry metricsRegistry) {
        this.metricsRegistry = metricsRegistry;
    }

    private final MetricsRegistry metricsRegistry;

    public MetricsDataDto getMetricsInfo() {
        return new MetricsDataDto(
            metricsRegistry.getAcceptedConnections(),
            metricsRegistry.getClosedConnections(),
            metricsRegistry.getRefusedConnections(),
            metricsRegistry.getCurrentClients(),
            metricsRegistry.getBytesIn(),
            metricsRegistry.getBytesOut(),
            metricsRegistry.getGetRequests(),
            metricsRegistry.getSetRequests(),
            metricsRegistry.getDeleteRequests(),
            metricsRegistry.getExitRequests(),
            metricsRegistry.getInfoRequests(),
            metricsRegistry.getPingRequests(),
            metricsRegistry.getUnknownMethods(),
            metricsRegistry.getHeaderTooLong(),
            metricsRegistry.getValueTooLong(),
            metricsRegistry.getKeyTooLong(),
            metricsRegistry.getUnexpectedEndOfFile(),
            metricsRegistry.getNonDigitInLength(),
            metricsRegistry.getInvalidSeparator(),
            metricsRegistry.getServerStart(),
            System.currentTimeMillis() - metricsRegistry.getServerStart()
        );
    }
}
