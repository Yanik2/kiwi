package com.kiwi.observability;

import com.kiwi.server.Method;

public final class MethodMetrics {
    private final MetricsRegistry metricsRegistry;

    MethodMetrics(MetricsRegistry metricsRegistry) {
        this.metricsRegistry = metricsRegistry;
    }

    public void onRequest(Method method) {
        switch (method) {
            case GET -> metricsRegistry.addGetRequest();
            case SET -> metricsRegistry.addSetRequest();
            case DEL -> metricsRegistry.addDeleteRequest();
            case EXT -> metricsRegistry.addExitRequest();
            case INF -> metricsRegistry.addInfoRequest();
            case PING -> metricsRegistry.addPingRequest();
            case EXPIRE -> metricsRegistry.addExpireRequest();
            case PEXPIRE -> metricsRegistry.addPexpireRequest();
            case PERSIST -> metricsRegistry.addPersistRequest();
        }
    }
}
