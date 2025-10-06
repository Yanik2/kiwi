package com.kiwi.server.response;

import com.kiwi.observability.dto.MetricsDataDto;
import java.nio.charset.StandardCharsets;

public record ObservabilityResponse(
    MetricsDataDto metrics
) implements SerializableValue {
    @Override
    public byte[] serialize() {
        return ("{ " +
            "\"con.accepted\": " +
            metrics.acceptedConnections() +
            ", \"con.closed\": " +
            metrics.closedConnections() +
            ", \"con.current\": " +
            metrics.currentClients() +
            ", \"bytes.in\": " +
            metrics.bytesIn() +
            ", \"bytes.out\": " +
            metrics.bytesOut() +
            ", \"cmd.get\": " +
            metrics.getRequests() +
            ", \"cmd.set\": " +
            metrics.setRequests() +
            ", \"cmd.del\": " +
            metrics.deleteRequests() +
            ", \"cmd.ext\": " +
            metrics.exitRequests() +
            ", \"cmd.inf\": " +
            metrics.infoRequests() +
            ", \"proto.err.unknown\": " +
            metrics.unknownMethod() +
            ", \"proto.err.headerlen\": " +
            metrics.headerTooLong() +
            ", \"proto.err.valuelen\": " +
            metrics.valueTooLong() +
            ", \"proto.err.keylen\": " +
            metrics.keyTooLong() +
            ", \"proto.err.eof\": " +
            metrics.unexpectedEndOfFile() +
            ", \"proto.err.nondigitlen\": " +
            metrics.nonDigitInLength() +
            ", \"proto.err.invalidseparator\": " +
            metrics.invalidSeparator() +
            " }")
            .getBytes(StandardCharsets.UTF_8);
    }
}
