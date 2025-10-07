package com.kiwi.observability;

public class ObservabilityModule {

    public static ObservabilityRequestHandler getRequestHandler() {
        return new ObservabilityRequestHandler(MetricsRegistry.getInstance());
    }

    public static RequestMetrics getRequestMetrics() {
        return new RequestMetrics(MetricsRegistry.getInstance());
    }

    public static MethodMetrics getMethodMetrics() {
        return new MethodMetrics(MetricsRegistry.getInstance());
    }

    public static StorageMetrics getStorageMetrics() {
        return new StorageMetrics(MetricsRegistry.getInstance());
    }
}
