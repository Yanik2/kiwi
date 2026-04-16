package com.kiwi.config.builder;

import com.kiwi.config.domain.Config;
import com.kiwi.exception.ConfigurationValidationException;

import static com.kiwi.config.properties.DefaultProperties.BACKLOG;
import static com.kiwi.config.properties.DefaultProperties.MAX_CLIENTS;
import static com.kiwi.config.properties.DefaultProperties.METRICS_ENABLED;
import static com.kiwi.config.properties.DefaultProperties.SERVER_PORT;
import static com.kiwi.config.properties.DefaultProperties.TIMEOUT_MILLIS;

public class ConfigBuilder {
    private int port = SERVER_PORT;
    private int backlog = BACKLOG;
    private int maxClients = MAX_CLIENTS;
    private int soTimeoutMillis = TIMEOUT_MILLIS;
    private boolean metricsEnabled = METRICS_ENABLED;

    public ConfigBuilder() {}

    public ConfigBuilder port(int port) {
        this.port = port;
        return this;
    }

    public ConfigBuilder backlog(int backlog) {
        this.backlog = backlog;
        return this;
    }

    public ConfigBuilder maxClients(int maxClients) {
        this.maxClients = maxClients;
        return this;
    }

    public ConfigBuilder soTimeoutMillis(int soTimeoutMillis) {
        this.soTimeoutMillis = soTimeoutMillis;
        return this;
    }

    public ConfigBuilder metricsEnabled(boolean metricsEnabled) {
        this.metricsEnabled = metricsEnabled;
        return this;
    }

    public Config build() {
        if (port < 0 || port > 65535) {
            throw new ConfigurationValidationException("Invalid port number: " + port);
        }
        if (backlog < 0) {
            throw new ConfigurationValidationException("Invalid backlog number: " + backlog);
        }
        if (maxClients <= 0) {
            throw new ConfigurationValidationException("Invalid max clients number: " + maxClients);
        }
        if (soTimeoutMillis < 0) {
            throw new ConfigurationValidationException("Invalid timeout number: " + soTimeoutMillis);
        }

        return new Config(this.port, this.backlog, this.maxClients, this.soTimeoutMillis, this.metricsEnabled);
    }
}
