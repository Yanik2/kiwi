package com.kiwi.server.dispatcher.command;

import com.kiwi.observability.MetricsProvider;
import com.kiwi.server.context.ConnectionContext;
import com.kiwi.server.dispatcher.OperationResult;
import com.kiwi.server.request.model.TCPRequest;
import com.kiwi.server.response.model.ObservabilityResponse;

public class InfoCommandHandler implements RequestCommandHandler {
    private final MetricsProvider metricsProvider;

    public InfoCommandHandler(MetricsProvider metricsProvider) {
        this.metricsProvider = metricsProvider;
    }

    @Override
    public OperationResult handle(TCPRequest request, ConnectionContext context) {
        final var metricsData = metricsProvider.getMetricsInfo();

        return new OperationResult(new ObservabilityResponse(metricsData), true);
    }
}
