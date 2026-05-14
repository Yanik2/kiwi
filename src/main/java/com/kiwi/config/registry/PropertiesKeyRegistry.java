package com.kiwi.config.registry;

import com.kiwi.exception.config.ConfigurationInitializationException;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static com.kiwi.config.util.ConfigConstants.CONFIG_FILE;
import static com.kiwi.config.util.ConfigConstants.EVICTION_POLICY;
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

                map.put(SERVER_PORT, new ConfigKey("server.port", "KV_SERVER_PORT", "8090", new ValueParser() {
                    public int getInt(String value) {
                        try {
                            return Integer.parseInt(value);
                        } catch (Exception ex) {
                            throw new ConfigurationInitializationException(
                                    "Error during parsing server.port: " + value, ex);
                        }
                    }
                }));
                map.put(SERVER_BACKLOG, new ConfigKey("server.backlog", "KV_SERVER_BACKLOG", "128", new ValueParser() {
                    public int getInt(String value) {
                        try {
                            return Integer.parseInt(value);
                        } catch (Exception ex) {
                            throw new ConfigurationInitializationException(
                                    "Error during parsing server.backlog: " + value, ex);
                        }
                    }
                }));
                map.put(SERVER_MAX_CLIENTS, new ConfigKey("server.maxClients", "KV_SERVER_MAX_CLIENTS", "1000", new ValueParser() {
                    public int getInt(String value) {
                        try {
                            return Integer.parseInt(value);
                        } catch (
                                Exception ex) {
                            throw new ConfigurationInitializationException(
                                    "Error during parsing server.maxClients: " + value, ex);
                        }
                    }
                }));
                map.put(SOCKET_TIMEOUT, new ConfigKey("socket.soTimeoutMillis", "KV_SOCKET_SOTIMEOUTMILLIS", "0", new ValueParser() {
                    public int getInt(String value) {
                        try {
                            return Integer.parseInt(value);
                        } catch (Exception ex) {
                            throw new ConfigurationInitializationException(
                                    "Error during parsing socket.soTimeoutMillis: " + value, ex);
                        }
                    }
                }));
                map.put(METRICS_ENABLED, new ConfigKey("metrics.enabled", "KV_METRICS_ENABLED", "true", new ValueParser() {
                    public boolean getBoolean(String value) {
                        if (!Boolean.TRUE.toString().equalsIgnoreCase(value)
                                && !Boolean.FALSE.toString().equalsIgnoreCase(value)) {
                            throw new ConfigurationInitializationException("Error during parsing metrics.enabled: "
                                    + value);
                        }

                        return Boolean.parseBoolean(value);
                    }
                }));
                map.put(CONFIG_FILE, new ConfigKey("kiwi.config", "KV_KIWI_CONFIG", "config/kiwi.properties", new ValueParser() {
                    public String getString(String value) {
                        return value;
                    }
                }));
                map.put(TTL_SAMPLER_PERIOD_MS, new ConfigKey(TTL_SAMPLER_PERIOD_MS, "KV_TTL_SAMPLER_PERIOD_MS", "1000", new ValueParser() {
                    public int getInt(String value) {
                        try {
                            return Integer.parseInt(value);
                        } catch (Exception ex) {
                            throw new ConfigurationInitializationException(
                                    "Error during parsing ttl.samplerPeriodMs: " + value, ex
                            );
                        }
                    }
                }));
                map.put(TTL_SAMPLE_BATCH, new ConfigKey(TTL_SAMPLE_BATCH, "KV_TTL_SAMPLE_BATCH", "100", new ValueParser() {
                    public int getInt(String value) {
                        try {
                            return Integer.parseInt(value);
                        } catch (Exception ex) {
                            throw new ConfigurationInitializationException(
                                    "Error during parsing ttl.sampleBatch: " + value, ex);
                        }
                    }
                }));
                map.put(TTL_BACKOFF_MAX_MS, new ConfigKey(TTL_BACKOFF_MAX_MS, "KV_TTL_BACKOFF_MAX_MS", "5000", new ValueParser() {
                    public int getInt(String value) {
                        try {
                            return Integer.parseInt(value);
                        } catch (Exception ex) {
                            throw new ConfigurationInitializationException(
                                    "Error during parsing ttl.backoffMaxMs: " + value, ex);
                        }
                    }
                }));
                // default value is temporary, until the actual logic is implemented
                map.put(MEMORY_MAX_BYTES, new ConfigKey(MEMORY_MAX_BYTES, "KV_MEMORY_MAX_BYTES", "1000", new ValueParser() {
                    public int getInt(String value) {
                        try {
                            return Integer.parseInt(value);
                        } catch (Exception ex) {
                            throw new ConfigurationInitializationException(
                                    "Error during parsing memory.maxBytes: " + value, ex);
                        }
                    }
                }));
                map.put(EVICTION_POLICY, new ConfigKey(EVICTION_POLICY, "KV_EVICTION_POLICY", "no_evict", new ValueParser() {
                    public String getString(String value) {
                        return value;
                    }
                }));

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
