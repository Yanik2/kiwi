package com.kiwi.server.response.model;

import com.kiwi.observability.dto.MetricsDataDto;

import static java.nio.charset.StandardCharsets.UTF_8;

public record ObservabilityResponse(
        MetricsDataDto metrics
) implements SerializableValue {
    @Override
    public byte[] serialize() {
        final var sb = new StringBuilder("{ \"proto.version\": " + metrics.protocolVersion() +
                ", \"proto.infoschemaversion\": " + metrics.infoSchemaVersion());

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
                .append(", \"config.server.port\": ")
                .append(metrics.config().port())
                .append(", \"config.server.backlog\": ")
                .append(metrics.config().backlog())
                .append(", \"config.server.maxclients\": ")
                .append(metrics.config().maxClients())
                .append(", \"config.socket.timeout\": ")
                .append(metrics.config().soTimeoutMillis())
                .append(", \"config.metrics.enabled\": ")
                .append(metrics.config().metricsEnabled())
                .append(" }")
                .toString()
                .getBytes(UTF_8);
    }
}
