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
            ", \"getRequests\": " +
            metrics.getRequests() +
            ", \"setRequests\": " +
            metrics.setRequests() +
            ", \"deleteRequests\": " +
            metrics.deleteRequests() +
            ", \"exitRequests\" " +
            metrics.exitRequests() +
            ", \"infoRequests\": " +
            metrics.infoRequests() +
            ", \"unknownRequests\" " +
            metrics.unknownRequests() +
            " }")
            .getBytes(StandardCharsets.UTF_8);
    }
}
