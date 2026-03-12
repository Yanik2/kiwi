package com.kiwi.observability;

import com.kiwi.server.request.Method;

import static com.kiwi.observability.util.MetricKeys.CMD_DEL;
import static com.kiwi.observability.util.MetricKeys.CMD_EXPIRE;
import static com.kiwi.observability.util.MetricKeys.CMD_EXT;
import static com.kiwi.observability.util.MetricKeys.CMD_GET;
import static com.kiwi.observability.util.MetricKeys.CMD_INF;
import static com.kiwi.observability.util.MetricKeys.CMD_PERSIST;
import static com.kiwi.observability.util.MetricKeys.CMD_PEXPIRE;
import static com.kiwi.observability.util.MetricKeys.CMD_PING;
import static com.kiwi.observability.util.MetricKeys.CMD_SET;

public final class MethodMetrics {
    private final MetricsRegistry metricsRegistry;

    public MethodMetrics(MetricsRegistry metricsRegistry) {
        this.metricsRegistry = metricsRegistry;

        metricsRegistry.registerCounter(CMD_GET);
        metricsRegistry.registerCounter(CMD_SET);
        metricsRegistry.registerCounter(CMD_DEL);
        metricsRegistry.registerCounter(CMD_EXT);
        metricsRegistry.registerCounter(CMD_INF);
        metricsRegistry.registerCounter(CMD_PING);
        metricsRegistry.registerCounter(CMD_EXPIRE);
        metricsRegistry.registerCounter(CMD_PEXPIRE);
        metricsRegistry.registerCounter(CMD_PERSIST);
    }

    public void onRequest(Method method) {
        switch (method) {
            case GET -> metricsRegistry.updateCounter(CMD_GET);
            case SET -> metricsRegistry.updateCounter(CMD_SET);
            case DEL -> metricsRegistry.updateCounter(CMD_DEL);
            case EXT -> metricsRegistry.updateCounter(CMD_EXT);
            case INF -> metricsRegistry.updateCounter(CMD_INF);
            case PING -> metricsRegistry.updateCounter(CMD_PING);
            case EXPIRE -> metricsRegistry.updateCounter(CMD_EXPIRE);
            case PEXPIRE -> metricsRegistry.updateCounter(CMD_PEXPIRE);
            case PERSIST -> metricsRegistry.updateCounter(CMD_PERSIST);
        }
    }
}
