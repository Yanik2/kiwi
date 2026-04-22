package com.kiwi.observability.metrics;

import com.kiwi.exception.protocol.ProtocolErrorCode;
import com.kiwi.observability.MetricsRegistry;

import static com.kiwi.observability.util.MetricKeys.CON_CURRENT;

public class RequestNoOpMetrics implements RequestMetrics {
    private final MetricsRegistry metricsRegistry;

    public RequestNoOpMetrics(MetricsRegistry metricsRegistry) {
        this.metricsRegistry = metricsRegistry;
        metricsRegistry.registerGauge(CON_CURRENT);
    }

    @Override
    public void onAccept() {

    }

    @Override
    public void onDrainTimeout() {

    }

    @Override
    public void onPendingResponse(int delta) {

    }

    @Override
    public void onConnection() {
        metricsRegistry.updateGauge(CON_CURRENT, 1);
    }

    @Override
    public void onReaderThreadActive(int delta) {

    }

    @Override
    public void onClose() {
        metricsRegistry.updateGauge(CON_CURRENT, -1);
    }

    @Override
    public void onParse(long bytes) {

    }

    @Override
    public void onWrite(long bytes) {

    }

    @Override
    public void onProtoError(ProtocolErrorCode protocolErrorCode) {

    }

    @Override
    public void onRefuse() {
        metricsRegistry.updateGauge(CON_CURRENT, -1);
    }

    @Override
    public long getCurrentClients() {
        return metricsRegistry.getGauge(CON_CURRENT);
    }
}
