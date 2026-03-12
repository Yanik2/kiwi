package com.kiwi.observability;

import com.kiwi.exception.protocol.ProtocolErrorCode;

import static com.kiwi.observability.util.MetricKeys.BYTES_IN;
import static com.kiwi.observability.util.MetricKeys.BYTES_OUT;
import static com.kiwi.observability.util.MetricKeys.CON_ACCEPTED;
import static com.kiwi.observability.util.MetricKeys.CON_CLOSED;
import static com.kiwi.observability.util.MetricKeys.CON_CURRENT;
import static com.kiwi.observability.util.MetricKeys.CON_DRAIN_TIMEOUTS;
import static com.kiwi.observability.util.MetricKeys.CON_PENDING_RESPONSES;
import static com.kiwi.observability.util.MetricKeys.CON_READER_THREAD_ACTIVE;
import static com.kiwi.observability.util.MetricKeys.CON_REFUSED;
import static com.kiwi.observability.util.MetricKeys.CON_TOTAL_CONNECTIONS;
import static com.kiwi.observability.util.MetricKeys.PROTO_ERR_BUFFER_ERROR;
import static com.kiwi.observability.util.MetricKeys.PROTO_ERR_EOF;
import static com.kiwi.observability.util.MetricKeys.PROTO_ERR_INVALID_HEADER;
import static com.kiwi.observability.util.MetricKeys.PROTO_ERR_INVALID_SEPARATOR;
import static com.kiwi.observability.util.MetricKeys.PROTO_ERR_NON_DIGIT_LEN;
import static com.kiwi.observability.util.MetricKeys.PROTO_ERR_UNKNOWN;
import static com.kiwi.observability.util.MetricKeys.PROTO_ERR_VALUE_LEN;
import static com.kiwi.observability.util.MetricKeys.PROTO_ERR_VALUE_TOO_SHORT;

public final class RequestMetrics {
    private final MetricsRegistry metricsRegistry;

    public RequestMetrics(MetricsRegistry metricsRegistry) {
        this.metricsRegistry = metricsRegistry;

        metricsRegistry.registerCounter(CON_ACCEPTED);
        metricsRegistry.registerCounter(CON_DRAIN_TIMEOUTS);
        metricsRegistry.registerGauge(CON_PENDING_RESPONSES);
        metricsRegistry.registerCounter(CON_TOTAL_CONNECTIONS);
        metricsRegistry.registerGauge(CON_READER_THREAD_ACTIVE);
        metricsRegistry.registerCounter(CON_CLOSED);
        metricsRegistry.registerCounter(BYTES_IN);
        metricsRegistry.registerCounter(BYTES_OUT);
        metricsRegistry.registerCounter(PROTO_ERR_UNKNOWN);
        metricsRegistry.registerCounter(PROTO_ERR_VALUE_LEN);
        metricsRegistry.registerCounter(PROTO_ERR_EOF);
        metricsRegistry.registerCounter(PROTO_ERR_NON_DIGIT_LEN);
        metricsRegistry.registerCounter(PROTO_ERR_INVALID_SEPARATOR);
        metricsRegistry.registerCounter(PROTO_ERR_VALUE_TOO_SHORT);
        metricsRegistry.registerCounter(PROTO_ERR_INVALID_HEADER);
        metricsRegistry.registerCounter(PROTO_ERR_BUFFER_ERROR);
        metricsRegistry.registerCounter(CON_REFUSED);
        metricsRegistry.registerGauge(CON_CURRENT);
    }

    public void onAccept() {
        metricsRegistry.updateCounter(CON_ACCEPTED);
    }

    public void onDrainTimeout() {
        metricsRegistry.updateCounter(CON_DRAIN_TIMEOUTS);
    }

    public void onPendingResponse(int delta) {
        metricsRegistry.updateGauge(CON_PENDING_RESPONSES, delta);
    }

    public void onConnection() {
        metricsRegistry.updateCounter(CON_TOTAL_CONNECTIONS);
        metricsRegistry.updateGauge(CON_CURRENT, 1);
    }

    public void onReaderThreadActive(int delta) {
        metricsRegistry.updateGauge(CON_READER_THREAD_ACTIVE, delta);
    }

    public void onClose() {
        metricsRegistry.updateCounter(CON_CLOSED);
        metricsRegistry.updateGauge(CON_CURRENT, -1);
    }

    public void onParse(long bytes) {
        metricsRegistry.updateCounter(BYTES_IN, bytes);
    }

    public void onWrite(long bytes) {
        metricsRegistry.updateCounter(BYTES_OUT, bytes);
    }

    public void onProtoError(ProtocolErrorCode protocolErrorCode) {
        switch (protocolErrorCode) {
            case UNKNOWN_METHOD -> metricsRegistry.updateCounter(PROTO_ERR_UNKNOWN);
            case VALUE_TOO_LONG -> metricsRegistry.updateCounter(PROTO_ERR_VALUE_LEN);
            case UNEXPECTED_EOF -> metricsRegistry.updateCounter(PROTO_ERR_EOF);
            case NON_DIGIT_IN_NUMERIC_VALUE -> metricsRegistry.updateCounter(PROTO_ERR_NON_DIGIT_LEN);
            case INVALID_SEPARATOR -> metricsRegistry.updateCounter(PROTO_ERR_INVALID_SEPARATOR);
            case VALUE_TOO_SHORT -> metricsRegistry.updateCounter(PROTO_ERR_VALUE_TOO_SHORT);
            case INVALID_HEADER -> metricsRegistry.updateCounter(PROTO_ERR_INVALID_HEADER);
            case BUFFER_ERROR -> metricsRegistry.updateCounter(PROTO_ERR_BUFFER_ERROR);
        }
    }

    public void onRefuse() {
        metricsRegistry.updateCounter(CON_REFUSED);
        metricsRegistry.updateGauge(CON_CURRENT, -1);
    }

    public long getCurrentClients() {
        return metricsRegistry.getGauge(CON_CURRENT);
    }
}
