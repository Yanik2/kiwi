package com.kiwi.config.registry;

import com.kiwi.exception.config.ConfigurationInitializationException;

import java.util.Map;
import java.util.Set;

import static com.kiwi.config.util.ConfigConstants.CONFIG_FILE;
import static com.kiwi.config.util.ConfigConstants.METRICS_ENABLED;
import static com.kiwi.config.util.ConfigConstants.SERVER_BACKLOG;
import static com.kiwi.config.util.ConfigConstants.SERVER_MAX_CLIENTS;
import static com.kiwi.config.util.ConfigConstants.SERVER_PORT;
import static com.kiwi.config.util.ConfigConstants.SOCKET_TIMEOUT;

public class PropertiesKeyRegistry {
    private static final PropertiesKeyRegistry instance = new PropertiesKeyRegistry();

    private final Map<String, ConfigKey> configKeys;

    private PropertiesKeyRegistry() {
        this.configKeys = Map.of(
                SERVER_PORT, new ConfigKey("server.port", "KV_SERVER_PORT", "8090", new ValueParser() {
                    public int getInt(String value) {
                        try {
                            return Integer.parseInt(value);
                        } catch (Exception ex) {
                            throw new ConfigurationInitializationException(
                                    "Error during parsing server.port: " + value, ex);
                        }
                    }
                }),


                SERVER_BACKLOG, new ConfigKey("server.backlog", "KV_SERVER_BACKLOG", "128", new ValueParser() {
                    public int getInt(String value) {
                        try {
                            return Integer.parseInt(value);
                        } catch (Exception ex) {
                            throw new ConfigurationInitializationException(
                                    "Error during parsing server.backlog: " + value, ex);
                        }
                    }
                }),
                SERVER_MAX_CLIENTS, new ConfigKey("server.maxClients", "KV_SERVER_MAX_CLIENTS", "1000", new ValueParser() {
                    public int getInt(String value) {
                        try {
                            return Integer.parseInt(value);
                        } catch (
                                Exception ex) {
                            throw new ConfigurationInitializationException(
                                    "Error during parsing server.maxClients: " + value, ex);
                        }
                    }
                }),
                SOCKET_TIMEOUT, new ConfigKey("socket.soTimeoutMillis", "KV_SOCKET_SOTIMEOUTMILLIS", "0", new ValueParser() {
                    public int getInt(String value) {
                        try {
                            return Integer.parseInt(value);
                        } catch (Exception ex) {
                            throw new ConfigurationInitializationException(
                                    "Error during parsing socket.soTimeoutMillis: " + value, ex);
                        }
                    }
                }),
                METRICS_ENABLED, new ConfigKey("metrics.enabled", "KV_METRICS_ENABLED", "true", new ValueParser() {
                    public boolean getBoolean(String value) {
                        if (!Boolean.TRUE.toString().equalsIgnoreCase(value)
                                && !Boolean.FALSE.toString().equalsIgnoreCase(value)) {
                            throw new ConfigurationInitializationException("Error during parsing metrics.enabled: "
                                    + value);
                        }

                        return Boolean.parseBoolean(value);
                    }
                }),
                CONFIG_FILE, new ConfigKey("kiwi.config", "KV_KIWI_CONFIG", "config/kiwi.properties", new ValueParser() {
                    public String getString(String value) {
                        return value;
                    }
                })
        );
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
