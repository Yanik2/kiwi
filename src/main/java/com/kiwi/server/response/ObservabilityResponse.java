package com.kiwi.server.response;

import com.kiwi.observability.dto.MetricsDataDto;
import java.nio.charset.StandardCharsets;

public record ObservabilityResponse(
    MetricsDataDto metrics
) implements SerializableValue {
    @Override
    public byte[] serialize() {
        return ("{ " +
            "\"acceptedConnections\": " +
            metrics.acceptedConnections() +
            ", \"closedConnections\": " +
            metrics.closedConnections() +
            ", \"currentClients\": " +
            metrics.currentClients() +
            ", \"bytesIn\": " +
            metrics.bytesIn() +
            ", \"bytesOut\": " +
            metrics.bytesOut() +
            " }")
            .getBytes(StandardCharsets.UTF_8);
    }
}
