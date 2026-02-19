package com.kiwi.observability.dto;

import java.util.Map;

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
    long expireRequests,
    long pexpireRequests,
    long persistRequests,
    long unknownMethod,
    long headerTooLong,
    long valueTooLong,
    long keyTooLong,
    long unexpectedEndOfFile,
    long nonDigitInLength,
    long invalidSeparator,
    long valueTooShort,
    long invalidHeader,
    long bufferError,
    long serverStart,
    long serverUptime,
    long ttlExpiredEviction,
    Map<String, ThreadPoolCountersDto> threadPoolsMetrics
) {
}
