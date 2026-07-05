package com.kiwi.config;

import com.kiwi.config.builder.ConfigBuilder;
import com.kiwi.config.domain.Config;
import com.kiwi.config.load.ConfigurationFileLoader;
import com.kiwi.config.load.DefaultSource;
import com.kiwi.config.load.EnvSource;
import com.kiwi.config.load.FileSource;
import com.kiwi.config.load.PropertySource;
import com.kiwi.config.load.SystemPropertiesSource;
import com.kiwi.config.provider.ConfigurationHolder;
import com.kiwi.config.registry.ConfigKey;
import com.kiwi.config.registry.PropertiesKeyRegistry;
import com.kiwi.config.registry.ValueParser;
import com.kiwi.exception.config.ConfigurationInitializationException;
import com.kiwi.log.KiwiLogger;
import com.kiwi.log.KiwiLoggerFactory;

import java.util.List;

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

public class ConfigModule {
    private static final KiwiLogger log = KiwiLoggerFactory.getLogger(ConfigModule.class.getName());

    private static final String DEFAULT_FILE_PATH = "config/kiwi.properties";

    private static ConfigurationHolder configurationHolder;

    public static Config createConfig() {
        final var systemSource = new SystemPropertiesSource();
        final var keyRegistry = PropertiesKeyRegistry.getInstance();
        var filePath = systemSource.load(keyRegistry.getKey(CONFIG_FILE));
        filePath = filePath == null ? DEFAULT_FILE_PATH : filePath;


        final var props = ConfigurationFileLoader.loadProperties(filePath);
        final var fileSource = new FileSource(props);
        final var defaultSource = new DefaultSource();
        final var envSource = new EnvSource();

        final var sources = List.of(systemSource, envSource, fileSource, defaultSource);
        final var builder = new ConfigBuilder();
        final var valueParser = new ValueParser();

        final var backlogKey = keyRegistry.getKey(SERVER_BACKLOG);
        final var backlogProperty = getProperty(sources, backlogKey);
        builder.backlog(valueParser.getInt(backlogProperty, "Error during parsing server.backlog: "));

        final var portKey = keyRegistry.getKey(SERVER_PORT);
        final var portProperty = getProperty(sources, portKey);
        builder.port(valueParser.getInt(portProperty, "Error during parsing server.port: "));

        final var maxClientsKey = keyRegistry.getKey(SERVER_MAX_CLIENTS);
        final var maxClientsProperty = getProperty(sources, maxClientsKey);
        builder.maxClients(valueParser.getInt(maxClientsProperty, "Error during parsing server.maxClients: "));

        final var socketTimeoutKey = keyRegistry.getKey(SOCKET_TIMEOUT);
        final var socketTimeoutProperty = getProperty(sources, socketTimeoutKey);
        builder.soTimeoutMillis(valueParser.getInt(socketTimeoutProperty, "Error during parsing socket.soTimeoutMillis: "));

        final var metricsEnabledKey = keyRegistry.getKey(METRICS_ENABLED);
        final var metricsEnabledProperty = getProperty(sources, metricsEnabledKey);
        builder.metricsEnabled(valueParser.getBoolean(metricsEnabledProperty, "Error during parsing metrics.enabled: "));

        final var ttlSamplerPeriodMsKey = keyRegistry.getKey(TTL_SAMPLER_PERIOD_MS);
        final var ttlSamplerPeriodMsProperty = getProperty(sources, ttlSamplerPeriodMsKey);
        builder.ttlSamplerPeriodMs(valueParser.getInt(ttlSamplerPeriodMsProperty, "Error during parsing ttl.samplerPeriodMs: "));

        final var ttlSampleBatchKey = keyRegistry.getKey(TTL_SAMPLE_BATCH);
        final var ttlSampleBatchProperty = getProperty(sources, ttlSampleBatchKey);
        builder.ttlSampleBatch(valueParser.getInt(ttlSampleBatchProperty, "Error during parsing ttl.sampleBatch: "));

        final var ttlBackoffMaxMsKey = keyRegistry.getKey(TTL_BACKOFF_MAX_MS);
        final var ttlBackoffMaxMsProperty = getProperty(sources, ttlBackoffMaxMsKey);
        builder.ttlBackoffMaxMs(valueParser.getInt(ttlBackoffMaxMsProperty, "Error during parsing ttl.backoffMaxMs: "));

        final var memoryMaxBytesKey = keyRegistry.getKey(MEMORY_MAX_BYTES);
        final var memoryMaxBytesProperty = getProperty(sources, memoryMaxBytesKey);
        builder.memoryMaxBytes(valueParser.getInt(memoryMaxBytesProperty, "Error during parsing memory.maxBytes: "));

        final var evictionPolicyKey = keyRegistry.getKey(EVICTION_POLICY);
        final var evictionPolicyProperty = getProperty(sources, evictionPolicyKey);
        builder.evictionPolicy(evictionPolicyProperty);

        final var jvmInfoEnabledKey = keyRegistry.getKey(JVM_INFO_ENABLED);
        final var jvmInfoEnabledProperty = getProperty(sources, jvmInfoEnabledKey);
        builder.jvmInfoEnabled(valueParser.getBoolean(jvmInfoEnabledProperty, "Error during parsing jvm info enabled: "));

        final var jvmJfrEnabledKey = keyRegistry.getKey(JVM_JFR_ENABLED);
        final var jvmJfrEnabledProperty = getProperty(sources, jvmJfrEnabledKey);
        builder.jvmJfrEnabled(valueParser.getBoolean(jvmJfrEnabledProperty, "Error during parsing jfr enabled: "));

        final var jvmJfrDirKey = keyRegistry.getKey(JVM_JFR_DIR);
        final var jvmJfrDirProperty = getProperty(sources, jvmJfrDirKey);
        builder.jvmJfrDir(jvmJfrDirProperty);

        final var jvmJfrMaxAgeSecondsKey = keyRegistry.getKey(JVM_JFR_MAX_AGE_SECONDS);
        final var jvmJfrMaxAgeSecondsProperty = getProperty(sources, jvmJfrMaxAgeSecondsKey);
        builder.jvmJfrMaxAgeSeconds(valueParser.getInt(jvmJfrMaxAgeSecondsProperty,
                "Error during parsing jfr max age seconds: "));

        final var jvmJfrMaxSizeBytesKey = keyRegistry.getKey(JVM_JFR_MAX_SIZE_BYTES);
        final var jvmJfrMaxSizeBytesProperty = getProperty(sources, jvmJfrMaxSizeBytesKey);
        builder.jvmJfrMaxSizeBytes(valueParser.getInt(jvmJfrMaxSizeBytesProperty,
                "Error during parsing jfr max size bytes: "));

        final var jvmBuffersStrategyKey = keyRegistry.getKey(JVM_BUFFERS_STRATEGY);
        final var jvmBuffersStrategyProperty = getProperty(sources, jvmBuffersStrategyKey);
        builder.jvmBuffersStrategy(jvmBuffersStrategyProperty);

        final var jvmBuffersPoolingEnabledKey = keyRegistry.getKey(JVM_BUFFERS_POOLING_ENABLED);
        final var jvmBuffersPoolingEnabledProperty = getProperty(sources, jvmBuffersPoolingEnabledKey);
        builder.jvmBuffersPoolingEnabled(valueParser.getBoolean(jvmBuffersPoolingEnabledProperty,
                "Error during parsing jvm buffers pooling enabled: "));

        final var jvmBuffersLeakTrackingEnabledKey = keyRegistry.getKey(JVM_BUFFERS_LEAK_TRACKING_ENABLED);
        final var jvmBuffersLeakTrackingEnabledProperty = getProperty(sources, jvmBuffersLeakTrackingEnabledKey);
        builder.jvmBuffersLeakTrackingEnabled(valueParser.getBoolean(jvmBuffersLeakTrackingEnabledProperty,
                "Error during parsing jvm buffers leak tracking enabled: "));

        final var jvmArenaEnabledKey = keyRegistry.getKey(JVM_ARENA_ENABLED);
        final var jvmArenaEnabledProperty = getProperty(sources, jvmArenaEnabledKey);
        builder.jvmArenaEnabled(valueParser.getBoolean(jvmArenaEnabledProperty,
                "Error during parsing jvm arena enabled: "));

        final var jvmArenaDebugPoisoningKey = keyRegistry.getKey(JVM_ARENA_DEBUG_POISONING);
        final var jvmArenaDebugPoisoningProperty = getProperty(sources, jvmArenaDebugPoisoningKey);
        builder.jvmArenaDebugPoisoning(valueParser.getBoolean(jvmArenaDebugPoisoningProperty,
                "Error during parsing jvm arena debug poisoning: "));

        final var jvmSafepointWatchdogEnabledKey = keyRegistry.getKey(JVM_SAFEPOINT_WATCHDOG_ENABLED);
        final var jvmSafepointWatchdogEnabledProperty = getProperty(sources, jvmSafepointWatchdogEnabledKey);
        builder.jvmSafepointWatchdogEnabled(valueParser.getBoolean(jvmSafepointWatchdogEnabledProperty,
                "Error during parsing jvm safepoint watchdog enabled: "));

        final var jvmSafepointWatchdogPeriodMsKey = keyRegistry.getKey(JVM_SAFEPOINT_WATCHDOG_PERIOD_MS);
        final var jvmSafepointWatchodPeriodMsProperty = getProperty(sources, jvmSafepointWatchdogPeriodMsKey);
        builder.jvmSafepointWatchdogPeriodMs(valueParser.getInt(jvmSafepointWatchodPeriodMsProperty,
                "Error during parsing jvm safepoint watchdog period ms: "));

        final var config = builder.build();
        logConfig(config);
        configurationHolder = new ConfigurationHolder(config);
        return config;
    }

    // this method is not thread safe and must be used only after configuration initialized
    // in future on hot config change will be made thread safe
    public static ConfigurationHolder getConfigurationHolder() {
        if (configurationHolder == null) {
            log.error("Attempt to get uninitialized configuration");
            throw new ConfigurationInitializationException("Configuration is not initialized");
        }
        return configurationHolder;
    }

    private static String getProperty(List<PropertySource> sources,
                                      ConfigKey configKey) {
        String value = null;
        for (PropertySource s : sources) {
            if (value == null) {
                value = s.load(configKey);
            }
        }

        return value;
    }

    private static void logConfig(Config config) {
        log.info("Kiwi configuration: \n" + SERVER_PORT + "=" + config.port() + "\n"
                + SERVER_BACKLOG + "=" + config.backlog() + "\n"
                + SERVER_MAX_CLIENTS + "=" + config.maxClients() + "\n"
                + SOCKET_TIMEOUT + "=" + config.soTimeoutMillis() + "\n"
                + METRICS_ENABLED + "=" + config.metricsEnabled() + "\n"
                + TTL_SAMPLER_PERIOD_MS + "=" + config.ttlSamplerPeriodMs() + "\n"
                + TTL_SAMPLE_BATCH + "=" + config.ttlSampleBatch() + "\n"
                + TTL_BACKOFF_MAX_MS + "=" + config.ttlBackoffMaxMs() + "\n"
                + MEMORY_MAX_BYTES + "=" + config.memoryMaxBytes() + "\n"
                + EVICTION_POLICY + "=" + config.evictionPolicy().getValue()
        );
    }
}
