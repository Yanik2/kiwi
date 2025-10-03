package com.kiwi.server.response;

import com.kiwi.observability.dto.MetricsDataDto;
import java.nio.charset.StandardCharsets;

public record ObservabilityResponse(
    MetricsDataDto metrics
) implements SerializableValue {
    @Override
    public byte[] serialize() {
        return new StringBuilder("{ ")
            .append("\"acceptedConnections\": ")
            .append(metrics.acceptedConnections())
            .append(", \"closedConnections\": ")
            .append(metrics.closedConnections())
            .append(", \"currentClients\": ")
            .append(metrics.currentClients())
            .append(", \"bytesIn\": ")
            .append(metrics.bytesIn())
            .append(", \"bytesOut\": ")
            .append(metrics.bytesOut())
            .append(" }")
            .toString()
            .getBytes(StandardCharsets.UTF_8);
    }
}
