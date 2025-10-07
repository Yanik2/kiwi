package com.kiwi.observability.dto;

public record MetricsDataDto(
    String protocolVersion,
    String infoSchemaVersion,
    long acceptedConnections,
    long closedConnections,
    long refusedConnections,
    long currentClients,
    long bytesIn,
    long bytesOut,
    long getRequests,
    long setRequests,
    long deleteRequests,
    long exitRequests,
    long infoRequests,
    long pingRequests,
    long unknownMethod,
    long headerTooLong,
    long valueTooLong,
    long keyTooLong,
    long unexpectedEndOfFile,
    long nonDigitInLength,
    long invalidSeparator,
    long serverStart,
    long serverUptime,
    long ttlExpiredEviction
) {
}
