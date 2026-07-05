package com.kiwi.config.util;

public final class ConfigConstants {
    public static final String SERVER_PORT = "server.port";
    public static final String SERVER_BACKLOG = "server.backlog";
    public static final String SERVER_MAX_CLIENTS = "server.maxClients";
    public static final String SOCKET_TIMEOUT = "socket.soTimeoutMillis";
    public static final String METRICS_ENABLED = "metrics.enabled";
    public static final String CONFIG_FILE = "kiwi.config";
    public static final String TTL_SAMPLER_PERIOD_MS = "ttl.samplerPeriodMs";
    public static final String TTL_SAMPLE_BATCH = "ttl.sampleBatch";
    public static final String TTL_BACKOFF_MAX_MS = "ttl.backoffMaxMs";
    public static final String MEMORY_MAX_BYTES = "memory.maxBytes";
    public static final String EVICTION_POLICY = "eviction.policy";

    public static final String JVM_INFO_ENABLED = "jvm.info.enabled";
    public static final String JVM_JFR_ENABLED = "jvm.jfr.enabled";
    public static final String JVM_JFR_DIR = "jvm.jfr.dir";
    public static final String JVM_JFR_MAX_AGE_SECONDS = "jvm.jfr.max_age_seconds";
    public static final String JVM_JFR_MAX_SIZE_BYTES = "jvm.jfr.max_size_bytes";
    public static final String JVM_BUFFERS_STRATEGY = "jvm.buffers.strategy";
    public static final String JVM_BUFFERS_POOLING_ENABLED = "jvm.buffers.pooling.enabled";
    public static final String JVM_BUFFERS_LEAK_TRACKING_ENABLED = "jvm.buffers.leak_tracking.enabled";
    public static final String JVM_ARENA_ENABLED = "jvm.arena.enabled";
    public static final String JVM_ARENA_DEBUG_POISONING = "jvm.arena.debug_poisoning";
    public static final String JVM_SAFEPOINT_WATCHDOG_ENABLED = "jvm.safepoint_watchdog.enabled";
    public static final String JVM_SAFEPOINT_WATCHDOG_PERIOD_MS = "jvm.safepoint_watchdog.period_ms";


    private ConfigConstants() {}
}
