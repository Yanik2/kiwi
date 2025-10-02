package com.kiwi.observability.dto;

public record MetricsDataDto(
    long acceptedConnections,
    long closedConnections,
    long currentClients
) {
}
