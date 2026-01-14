package com.kiwi.observability;

public class ObservabilityModule {

    public static MetricsProvider getRequestHandler() {
        return new MetricsProvider(MetricsRegistry.getInstance());
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

    public static ThreadPoolMetrics getThreadPoolMetrics(String threadPoolName) {
        return new ThreadPoolMetrics(MetricsRegistry.getInstance(), threadPoolName);
    }
}
