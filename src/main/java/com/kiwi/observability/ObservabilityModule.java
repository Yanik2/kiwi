package com.kiwi.observability;

public class ObservabilityModule {

    public static ObservabilityRequestHandler getRequestHandler() {
        return new ObservabilityRequestHandler(MetricsRegistry.getInstance());
    }

    public static RequestMetrics getRequestMetrics() {
        return new RequestMetrics(MetricsRegistry.getInstance());
    }
}
