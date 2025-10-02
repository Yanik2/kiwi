package com.kiwi.observability;

public class ObservabilityModule {
    public static Metrics getMetrics() {
        return Metrics.getInstance();
    }

    public static ObservabilityRequestHandler getRequestHandler() {
        return new ObservabilityRequestHandler(getMetrics());
    }
}
