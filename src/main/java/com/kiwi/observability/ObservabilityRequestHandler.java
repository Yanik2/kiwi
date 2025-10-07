package com.kiwi.observability;

import com.kiwi.config.properties.ProtocolProperties;
import com.kiwi.observability.dto.MetricsDataDto;

public class ObservabilityRequestHandler {
    ObservabilityRequestHandler(MetricsRegistry metricsRegistry) {
        this.metricsRegistry = metricsRegistry;
    }

    private final MetricsRegistry metricsRegistry;

    public MetricsDataDto getMetricsInfo() {
        return new MetricsDataDto(
            ProtocolProperties.PROTOCOL_VERSION,
            ProtocolProperties.INFO_SCHEMA_VERSION,
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
            System.currentTimeMillis() - metricsRegistry.getServerStart(),
            metricsRegistry.getTtlExpiredEviction()
        );
    }
}
