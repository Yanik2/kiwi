package com.kiwi.config.builder;

import com.kiwi.config.domain.Config;
import com.kiwi.config.domain.EvictionPolicy;
import com.kiwi.config.domain.JvmBuffersStrategy;
import com.kiwi.config.domain.JvmConfig;
import com.kiwi.exception.config.ConfigurationValidationException;

import static com.kiwi.config.properties.DefaultProperties.BACKLOG;
import static com.kiwi.config.properties.DefaultProperties.EVICTION_POLICY;
import static com.kiwi.config.properties.DefaultProperties.MAX_CLIENTS;
import static com.kiwi.config.properties.DefaultProperties.MEMORY_MAX_BYTES;
import static com.kiwi.config.properties.DefaultProperties.METRICS_ENABLED;
import static com.kiwi.config.properties.DefaultProperties.SERVER_PORT;
import static com.kiwi.config.properties.DefaultProperties.TIMEOUT_MILLIS;
import static com.kiwi.config.properties.DefaultProperties.TTL_BACKOFF_MAX_MS;
import static com.kiwi.config.properties.DefaultProperties.TTL_SAMPLER_PERIOD_MS;
import static com.kiwi.config.properties.DefaultProperties.TTL_SAMPLE_BATCH;

public class ConfigBuilder {
    private int port = SERVER_PORT;
    private int backlog = BACKLOG;
    private int maxClients = MAX_CLIENTS;
    private int soTimeoutMillis = TIMEOUT_MILLIS;
    private boolean metricsEnabled = METRICS_ENABLED;
    private int ttlSamplerPeriodMs = TTL_SAMPLER_PERIOD_MS;
    private int ttlSampleBatch = TTL_SAMPLE_BATCH;
    private int ttlBackoffMaxMs = TTL_BACKOFF_MAX_MS;
    private int memoryMaxBytes = MEMORY_MAX_BYTES;
    private String evictionPolicy = EVICTION_POLICY;
    private boolean jvmInfoEnabled;
    private boolean jvmJfrEnabled;
    private String jvmJfrDir;
    private int jvmJfrMaxAgeSeconds;
    private int jvmJfrMaxSizeBytes;
    private String jvmBuffersStrategy;
    private boolean jvmBuffersPoolingEnabled;
    private boolean jvmBuffersLeakTrackingEnabled;
    private boolean jvmArenaEnabled;
    private boolean jvmArenaDebugPoisoning;
    private boolean jvmSafepointWatchdogEnabled;
    private int jvmSafepointWatchdogPeriodMs;


    public ConfigBuilder() {}

    public ConfigBuilder jvmSafepointWatchdogPeriodMs(int safepointWatchdogPeriodMs) {
        this.jvmSafepointWatchdogPeriodMs = safepointWatchdogPeriodMs;
        return this;
    }

    public ConfigBuilder jvmSafepointWatchdogEnabled(boolean safepointWatchdogEnabled) {
        this.jvmSafepointWatchdogEnabled = safepointWatchdogEnabled;
        return this;
    }

    public ConfigBuilder jvmArenaDebugPoisoning(boolean arenaDebugPoisoning) {
        this.jvmArenaDebugPoisoning = arenaDebugPoisoning;
        return this;
    }

    public ConfigBuilder jvmArenaEnabled(boolean arenaEnabled) {
        this.jvmArenaEnabled = arenaEnabled;
        return this;
    }

    public ConfigBuilder jvmBuffersLeakTrackingEnabled(boolean leakTrackingEnabled) {
        this.jvmBuffersLeakTrackingEnabled = leakTrackingEnabled;
        return this;
    }

    public ConfigBuilder jvmBuffersPoolingEnabled(boolean poolingEnabled) {
        this.jvmBuffersPoolingEnabled = poolingEnabled;
        return this;
    }

    public ConfigBuilder jvmBuffersStrategy(String bufferStrategy) {
        this.jvmBuffersStrategy = bufferStrategy;
        return this;
    }

    public ConfigBuilder jvmJfrMaxSizeBytes(int maxSizeBytes) {
        this.jvmJfrMaxSizeBytes = maxSizeBytes;
        return this;
    }

    public ConfigBuilder jvmJfrMaxAgeSeconds(int maxAgeSeconds) {
        this.jvmJfrMaxAgeSeconds = maxAgeSeconds;
        return this;
    }

    public ConfigBuilder jvmJfrDir(String jfrDir) {
        this.jvmJfrDir = jfrDir;
        return this;
    }

    public ConfigBuilder jvmJfrEnabled(boolean enabled) {
        this.jvmJfrEnabled = enabled;
        return this;
    }

    public ConfigBuilder jvmInfoEnabled(boolean enabled) {
        this.jvmInfoEnabled = enabled;
        return this;
    }

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

    public ConfigBuilder ttlSamplerPeriodMs(int ttlSamplerPeriodMs) {
        this.ttlSamplerPeriodMs = ttlSamplerPeriodMs;
        return this;
    }

    public ConfigBuilder ttlSampleBatch(int ttlSampleBatch) {
        this.ttlSampleBatch = ttlSampleBatch;
        return this;
    }

    public ConfigBuilder ttlBackoffMaxMs(int ttlBackoffMaxMs) {
        this.ttlBackoffMaxMs = ttlBackoffMaxMs;
        return this;
    }

    public ConfigBuilder memoryMaxBytes(int memoryMaxBytes) {
        this.memoryMaxBytes = memoryMaxBytes;
        return this;
    }

    public ConfigBuilder evictionPolicy(String evictionPolicy) {
        this.evictionPolicy = evictionPolicy;
        return this;
    }

    public Config build() {
        if (port <= 0 || port > 65535) {
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
        if (ttlSamplerPeriodMs <= 0) {
            throw new ConfigurationValidationException("Invalid ttl sampler period: " + ttlSamplerPeriodMs);
        }
        if (ttlSampleBatch <= 0) {
            throw new ConfigurationValidationException("Invalid ttl sample batch: " + ttlSampleBatch);
        }
        if (ttlBackoffMaxMs < ttlSamplerPeriodMs) {
            throw new ConfigurationValidationException("Invalid ttl backoff max ms: " + ttlBackoffMaxMs);
        }
        if (memoryMaxBytes < 0) {
            throw new ConfigurationValidationException("Invalid memory max bytes: " + memoryMaxBytes);
        }
        if (!EvictionPolicy.exists(evictionPolicy)) {
            throw new ConfigurationValidationException("Invalid eviction policy: " + evictionPolicy);
        }
        if (jvmJfrDir.isBlank()) {
            throw new ConfigurationValidationException("Jfr directory cannot be empty");
        }
        if (jvmJfrMaxAgeSeconds < 1) {
            throw new ConfigurationValidationException("Invalid java flight recorder age: " + jvmJfrMaxAgeSeconds);
        }
        if (jvmJfrMaxSizeBytes < 1) {
            throw new ConfigurationValidationException("Invalid java flight recorder max size: " + jvmJfrMaxSizeBytes);
        }
        if (JvmBuffersStrategy.exists(jvmBuffersStrategy.toLowerCase())) {
            throw new ConfigurationValidationException("Invalid java buffers strategy: " + jvmBuffersStrategy);
        }
        if (jvmSafepointWatchdogPeriodMs < 10) {
            throw new ConfigurationValidationException("Invalid java safepoint watchdog period: "
                    + jvmSafepointWatchdogPeriodMs);
        }

        return new Config(this.port, this.backlog, this.maxClients, this.soTimeoutMillis, this.metricsEnabled,
                ttlSamplerPeriodMs, ttlSampleBatch, ttlBackoffMaxMs, memoryMaxBytes, EvictionPolicy.get(evictionPolicy),
                new JvmConfig(this.jvmInfoEnabled, this.jvmJfrEnabled, this.jvmJfrDir, this.jvmJfrMaxAgeSeconds,
                        this.jvmJfrMaxSizeBytes, JvmBuffersStrategy.valueOf(this.jvmBuffersStrategy.toUpperCase()),
                        this.jvmBuffersPoolingEnabled, this.jvmBuffersLeakTrackingEnabled, this.jvmArenaEnabled,
                        this.jvmArenaDebugPoisoning, this.jvmSafepointWatchdogEnabled, this.jvmSafepointWatchdogPeriodMs));
    }
}
