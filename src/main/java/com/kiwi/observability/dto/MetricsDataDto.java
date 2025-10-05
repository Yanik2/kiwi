package com.kiwi.observability.dto;

public record MetricsDataDto(
    long acceptedConnections,
    long closedConnections,
    long currentClients,
    long bytesIn,
    long bytesOut,
    long getRequests,
    long setRequests,
    long deleteRequests,
    long exitRequests,
    long infoRequests,
    long unknownRequests
) {
}
