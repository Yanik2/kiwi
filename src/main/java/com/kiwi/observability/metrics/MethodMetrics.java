package com.kiwi.observability.metrics;

import com.kiwi.observability.MetricsRegistry;
import com.kiwi.server.request.Method;

import static com.kiwi.observability.util.MetricKeys.CMD_DBSIZE;
import static com.kiwi.observability.util.MetricKeys.CMD_DECR;
import static com.kiwi.observability.util.MetricKeys.CMD_DECRBY;
import static com.kiwi.observability.util.MetricKeys.CMD_DEL;
import static com.kiwi.observability.util.MetricKeys.CMD_EXISTS;
import static com.kiwi.observability.util.MetricKeys.CMD_EXPIRE;
import static com.kiwi.observability.util.MetricKeys.CMD_EXT;
import static com.kiwi.observability.util.MetricKeys.CMD_GET;
import static com.kiwi.observability.util.MetricKeys.CMD_GETSET;
import static com.kiwi.observability.util.MetricKeys.CMD_INCR;
import static com.kiwi.observability.util.MetricKeys.CMD_INCRBY;
import static com.kiwi.observability.util.MetricKeys.CMD_INF;
import static com.kiwi.observability.util.MetricKeys.CMD_MGET;
import static com.kiwi.observability.util.MetricKeys.CMD_MSET;
import static com.kiwi.observability.util.MetricKeys.CMD_PERSIST;
import static com.kiwi.observability.util.MetricKeys.CMD_PEXPIRE;
import static com.kiwi.observability.util.MetricKeys.CMD_PING;
import static com.kiwi.observability.util.MetricKeys.CMD_SET;
import static com.kiwi.observability.util.MetricKeys.CMD_SETNX;

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
        metricsRegistry.registerCounter(CMD_EXISTS);
        metricsRegistry.registerCounter(CMD_SETNX);
        metricsRegistry.registerCounter(CMD_GETSET);
        metricsRegistry.registerCounter(CMD_INCR);
        metricsRegistry.registerCounter(CMD_DECR);
        metricsRegistry.registerCounter(CMD_INCRBY);
        metricsRegistry.registerCounter(CMD_DECRBY);
        metricsRegistry.registerCounter(CMD_MGET);
        metricsRegistry.registerCounter(CMD_MSET);
        metricsRegistry.registerCounter(CMD_DBSIZE);
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
            case EXISTS -> metricsRegistry.updateCounter(CMD_EXISTS);
            case SETNX -> metricsRegistry.updateCounter(CMD_SETNX);
            case GETSET -> metricsRegistry.updateCounter(CMD_GETSET);
            case INCR -> metricsRegistry.updateCounter(CMD_INCR);
            case DECR -> metricsRegistry.updateCounter(CMD_DECR);
            case INCRBY -> metricsRegistry.updateCounter(CMD_INCRBY);
            case DECRBY -> metricsRegistry.updateCounter(CMD_DECRBY);
            case MGET -> metricsRegistry.updateCounter(CMD_MGET);
            case MSET -> metricsRegistry.updateCounter(CMD_MSET);
            case DBSIZE -> metricsRegistry.updateCounter(CMD_DBSIZE);
        }
    }
}
