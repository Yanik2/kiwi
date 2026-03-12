package com.kiwi.server.response.model;

import com.kiwi.observability.dto.MetricsDataDto;

import static java.nio.charset.StandardCharsets.UTF_8;

public record ObservabilityResponse(
        MetricsDataDto metrics
) implements SerializableValue {
    @Override
    public byte[] serialize() {
        final var sb = new StringBuilder("{ \"proto.version\": " + metrics.protocolVersion() +
                ", \"proto.schemaversion\": " + metrics.infoSchemaVersion());

        metrics.gauges().forEach((k, v) -> sb.append(", \"")
                .append(k)
                .append("\": ")
                .append(v));
        metrics.counters().forEach((k, v) -> sb.append(", \"")
                .append(k)
                .append("\": ")
                .append(v));
        return sb.append(", \"server.start\": ")
                .append(metrics.serverStart())
                .append(", \"server.uptime\": ")
                .append(metrics.serverUptime())
                .append(" }")
                .toString()
                .getBytes(UTF_8);
    }
}
