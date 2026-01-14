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
                ", \"con.refused\": " +
                metrics.refusedConnections() +
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
                ", \"cmd.ping\": " +
                metrics.pingRequests() +
                ", \"cmd.expire\": " +
                metrics.expireRequests() +
                ", \"cmd.pexpire\": " +
                metrics.pexpireRequests() +
                ", \"cmd.persist\": " +
                metrics.persistRequests() +
                ", \"proto.version\": " +
                metrics.protocolVersion() +
                ", \"proto.infoschemaversion\": " +
                metrics.infoSchemaVersion() +
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
                ", \"proto.err.valuetooshort\": " +
                metrics.valueTooShort() +
                ", \"server.start\": " +
                metrics.serverStart() +
                ", \"server.uptime\": " +
                metrics.serverUptime() +
                ", \"storage.ttl.expired.eviction\": " +
                metrics.ttlExpiredEviction() +
                serializeThreadPoolMetrics() +
                " }")
                .getBytes(StandardCharsets.UTF_8);
    }

    private String serializeThreadPoolMetrics() {
        final var sb = new StringBuilder();
        metrics.threadPoolsMetrics().forEach((k, v) -> {
            final var prefix = ", \"tp." + k;
            sb.append(prefix)
                    .append(".workers_max\": ")
                    .append(v.workersMax())
                    .append(prefix)
                    .append(".workers_active\": ")
                    .append(v.workersActive())
                    .append(prefix)
                    .append(".queue_size\": ")
                    .append(v.queueSize())
                    .append(prefix)
                    .append(".task_enqueued\": ")
                    .append(v.taskEnqueued())
                    .append(prefix)
                    .append(".task_completed\": ")
                    .append(v.taskCompleted())
                    .append(prefix)
                    .append(".task_rejected\": ")
                    .append(v.taskRejected());
        });
        return sb.toString();
    }
}
