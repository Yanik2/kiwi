package com.kiwi.observability;

public final class MethodMetrics {
    private final MetricsRegistry metricsRegistry;

    MethodMetrics(MetricsRegistry metricsRegistry) {
        this.metricsRegistry = metricsRegistry;
    }

    public void onGet() {
        metricsRegistry.addGetRequest();
    }

    public void onSet() {
        metricsRegistry.addSetRequest();
    }

    public void onDelete() {
        metricsRegistry.addDeleteRequest();
    }

    public void onExit() {
        metricsRegistry.addExitRequest();
    }

    public void onInfo() {
        metricsRegistry.addInfoRequest();
    }

    public void onUnknown() {
        metricsRegistry.addUnknownRequest();
    }
}
