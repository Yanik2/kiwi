package com.kiwi.config.registry;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static com.kiwi.config.util.ConfigConstants.CONFIG_FILE;
import static com.kiwi.config.util.ConfigConstants.EVICTION_POLICY;
import static com.kiwi.config.util.ConfigConstants.JVM_ARENA_DEBUG_POISONING;
import static com.kiwi.config.util.ConfigConstants.JVM_ARENA_ENABLED;
import static com.kiwi.config.util.ConfigConstants.JVM_BUFFERS_LEAK_TRACKING_ENABLED;
import static com.kiwi.config.util.ConfigConstants.JVM_BUFFERS_POOLING_ENABLED;
import static com.kiwi.config.util.ConfigConstants.JVM_BUFFERS_STRATEGY;
import static com.kiwi.config.util.ConfigConstants.JVM_INFO_ENABLED;
import static com.kiwi.config.util.ConfigConstants.JVM_JFR_DIR;
import static com.kiwi.config.util.ConfigConstants.JVM_JFR_ENABLED;
import static com.kiwi.config.util.ConfigConstants.JVM_JFR_MAX_AGE_SECONDS;
import static com.kiwi.config.util.ConfigConstants.JVM_JFR_MAX_SIZE_BYTES;
import static com.kiwi.config.util.ConfigConstants.JVM_SAFEPOINT_WATCHDOG_ENABLED;
import static com.kiwi.config.util.ConfigConstants.JVM_SAFEPOINT_WATCHDOG_PERIOD_MS;
import static com.kiwi.config.util.ConfigConstants.MEMORY_MAX_BYTES;
import static com.kiwi.config.util.ConfigConstants.METRICS_ENABLED;
import static com.kiwi.config.util.ConfigConstants.SERVER_BACKLOG;
import static com.kiwi.config.util.ConfigConstants.SERVER_MAX_CLIENTS;
import static com.kiwi.config.util.ConfigConstants.SERVER_PORT;
import static com.kiwi.config.util.ConfigConstants.SOCKET_TIMEOUT;
import static com.kiwi.config.util.ConfigConstants.TTL_BACKOFF_MAX_MS;
import static com.kiwi.config.util.ConfigConstants.TTL_SAMPLER_PERIOD_MS;
import static com.kiwi.config.util.ConfigConstants.TTL_SAMPLE_BATCH;

public class PropertiesKeyRegistry {
    private static final PropertiesKeyRegistry instance = new PropertiesKeyRegistry();

    private final Map<String, ConfigKey> configKeys;

    private PropertiesKeyRegistry() {
        final var map = new HashMap<String, ConfigKey>();

                map.put(SERVER_PORT, new ConfigKey("server.port", "KV_SERVER_PORT", "8090"));
                map.put(SERVER_BACKLOG, new ConfigKey("server.backlog", "KV_SERVER_BACKLOG", "128"));
                map.put(SERVER_MAX_CLIENTS, new ConfigKey("server.maxClients", "KV_SERVER_MAX_CLIENTS", "1000"));
                map.put(SOCKET_TIMEOUT, new ConfigKey("socket.soTimeoutMillis", "KV_SOCKET_SOTIMEOUTMILLIS", "0"));
                map.put(METRICS_ENABLED, new ConfigKey("metrics.enabled", "KV_METRICS_ENABLED", "true"));
                map.put(CONFIG_FILE, new ConfigKey("kiwi.config", "KV_KIWI_CONFIG", "config/kiwi.properties"));
                map.put(TTL_SAMPLER_PERIOD_MS, new ConfigKey(TTL_SAMPLER_PERIOD_MS, "KV_TTL_SAMPLER_PERIOD_MS", "1000"));
                map.put(TTL_SAMPLE_BATCH, new ConfigKey(TTL_SAMPLE_BATCH, "KV_TTL_SAMPLE_BATCH", "100"));
                map.put(TTL_BACKOFF_MAX_MS, new ConfigKey(TTL_BACKOFF_MAX_MS, "KV_TTL_BACKOFF_MAX_MS", "5000"));
                // TODO count default memory max bytes from heap size
                map.put(MEMORY_MAX_BYTES, new ConfigKey(MEMORY_MAX_BYTES, "KV_MEMORY_MAX_BYTES", "1000"));
                map.put(EVICTION_POLICY, new ConfigKey(EVICTION_POLICY, "KV_EVICTION_POLICY", "no_evict"));

                // jvm
                map.put(JVM_INFO_ENABLED, new ConfigKey(JVM_INFO_ENABLED, "KV_JVM_INFO_ENABLED", "false"));
                map.put(JVM_JFR_ENABLED, new ConfigKey(JVM_JFR_ENABLED, "KV_JVM_JFR_ENABLED", "false"));
                map.put(JVM_JFR_DIR, new ConfigKey(JVM_JFR_DIR, "KV_JVM_JFR_DIR", "./jfr"));
                map.put(JVM_JFR_MAX_AGE_SECONDS, new ConfigKey(JVM_JFR_MAX_AGE_SECONDS,
                        "KV_JVM_JFR_MAX_AGE_SECONDS", "3600"));
                map.put(JVM_JFR_MAX_SIZE_BYTES, new ConfigKey(JVM_JFR_MAX_SIZE_BYTES,
                        "KV_JVM_JFR_MAX_SIZE_BYTES", "104857600"));
                map.put(JVM_BUFFERS_STRATEGY, new ConfigKey(JVM_BUFFERS_STRATEGY, "KV_JVM_BUFFERS_STRATEGY", "heap"));
                map.put(JVM_BUFFERS_POOLING_ENABLED, new ConfigKey(JVM_BUFFERS_POOLING_ENABLED,
                        "KV_JVM_BUFFERS_POOLING_ENABLED", "false"));
                map.put(JVM_BUFFERS_LEAK_TRACKING_ENABLED, new ConfigKey(JVM_BUFFERS_LEAK_TRACKING_ENABLED,
                        "KV_JVM_BUFFERS_LEAK_TRACKING_ENABLED", "false"));
                map.put(JVM_ARENA_ENABLED, new ConfigKey(JVM_ARENA_ENABLED, "KV_JVM_ARENA_ENABLED", "false"));
                map.put(JVM_ARENA_DEBUG_POISONING, new ConfigKey(JVM_ARENA_DEBUG_POISONING,
                        "KV_JVM_ARENA_DEBUG_POISONING", "false"));
                map.put(JVM_SAFEPOINT_WATCHDOG_ENABLED, new ConfigKey(JVM_SAFEPOINT_WATCHDOG_ENABLED,
                        "KV_JVM_SAFEPOINT_WATCHDOG_ENABLED", "false"));
                map.put(JVM_SAFEPOINT_WATCHDOG_PERIOD_MS, new ConfigKey(JVM_SAFEPOINT_WATCHDOG_PERIOD_MS,
                        "KV_JVM_SAFEPOINT_WATCHDOG_PERIOD_MS", "100"));

                this.configKeys = Collections.unmodifiableMap(map);
    }

    public static PropertiesKeyRegistry getInstance() {
        return instance;
    }

    public ConfigKey getKey(String keyName) {
        return configKeys.get(keyName);
    }

    public Set<String> getKeyNames() {
        return configKeys.keySet();
    }
}
