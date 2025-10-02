package com.kiwi.server.response;

import com.kiwi.observability.dto.MetricsDataDto;
import java.nio.charset.StandardCharsets;

public record ObservabilityResponse(
    MetricsDataDto metrics
) implements SerializableValue {
    @Override
    public byte[] serialize() {
        final var sb = new StringBuilder("{ ");
        sb.append("\"acceptedConnections\": ");
        sb.append(metrics.acceptedConnections());
        sb.append(", \"closedConnections\": ");
        sb.append(metrics.closedConnections());
        sb.append(", \"currentClients\": ");
        sb.append(metrics.currentClients());
        sb.append(" }");
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }
}
