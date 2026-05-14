package com.kiwi.config.properties;

public final class DefaultProperties {
    public static final int SERVER_PORT = 8090;
    public static final int BACKLOG = 128;
    public static final int MAX_CLIENTS = 1000;
    public static final int TIMEOUT_MILLIS = 0;
    public static final boolean METRICS_ENABLED = true;
    public static final int TTL_SAMPLER_PERIOD_MS = 1000;
    public static final int TTL_SAMPLE_BATCH = 100;
    public static final int TTL_BACKOFF_MAX_MS = 5000;
    public static final int MEMORY_MAX_BYTES = 1000;
    public static final String EVICTION_POLICY = "no_evict";

    private DefaultProperties() {}
}
