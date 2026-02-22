package com.kiwi.server.dispatcher.command;

import com.kiwi.observability.MetricsProvider;
import com.kiwi.server.context.ConnectionContext;
import com.kiwi.server.dto.TCPRequest;
import com.kiwi.server.response.ObservabilityResponse;
import com.kiwi.server.response.SerializableValue;

public class InfoCommandHandler implements RequestCommandHandler {
    private final MetricsProvider metricsProvider;

    public InfoCommandHandler(MetricsProvider metricsProvider) {
        this.metricsProvider = metricsProvider;
    }

    @Override
    public SerializableValue handle(TCPRequest request, ConnectionContext context) {
        final var metricsData = metricsProvider.getMetricsInfo();

        return new ObservabilityResponse(metricsData);
    }
}
