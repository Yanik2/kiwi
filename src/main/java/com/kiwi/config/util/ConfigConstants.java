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


    private ConfigConstants() {}
}
