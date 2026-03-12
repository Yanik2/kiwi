package com.kiwi.observability;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class MetricsRegistry {
    private static final MetricsRegistry instance = new MetricsRegistry();

    private final ConcurrentMap<String, Integer> gauges = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Long> counters = new ConcurrentHashMap<>();

    private final long startUpMillis;

    private MetricsRegistry() {
        this.startUpMillis = System.currentTimeMillis();
    }

    public void registerGauge(String name) {
        gauges.putIfAbsent(name, 0);
    }

    public void registerCounter(String name) {
        counters.putIfAbsent(name, 0L);
    }

    public void updateGauge(String name, int delta) {
        gauges.merge(name, delta, Integer::sum);
    }

    public void updateCounter(String name) {
        updateCounter(name, 1L);
    }

    public void updateCounter(String name, long delta) {
        counters.merge(name, delta, Long::sum);
    }

    public Map<String, Integer> getGauges() {
        return Collections.unmodifiableMap(gauges);
    }

    public Map<String, Long> getCounters() {
        return Collections.unmodifiableMap(counters);
    }

    public static MetricsRegistry getInstance() {
        return instance;
    }

    public long getServerStart() {
        return startUpMillis;
    }

    public long getGauge(String name) {
        return gauges.get(name);
    }
}