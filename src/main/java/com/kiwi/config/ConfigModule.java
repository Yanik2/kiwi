package com.kiwi.config;

import com.kiwi.config.builder.ConfigBuilder;
import com.kiwi.config.domain.Config;
import com.kiwi.config.load.ConfigurationFileLoader;
import com.kiwi.config.load.DefaultSource;
import com.kiwi.config.load.EnvSource;
import com.kiwi.config.load.FileSource;
import com.kiwi.config.load.PropertySource;
import com.kiwi.config.load.SystemPropertiesSource;
import com.kiwi.config.registry.ConfigKey;
import com.kiwi.config.registry.PropertiesKeyRegistry;

import java.util.List;
import java.util.logging.Logger;

import static com.kiwi.config.util.ConfigConstants.CONFIG_FILE;
import static com.kiwi.config.util.ConfigConstants.METRICS_ENABLED;
import static com.kiwi.config.util.ConfigConstants.SERVER_BACKLOG;
import static com.kiwi.config.util.ConfigConstants.SERVER_MAX_CLIENTS;
import static com.kiwi.config.util.ConfigConstants.SERVER_PORT;
import static com.kiwi.config.util.ConfigConstants.SOCKET_TIMEOUT;

public class ConfigModule {
    private static final Logger log = Logger.getLogger(ConfigModule.class.getName());

    private static final String DEFAULT_FILE_PATH = "config/kiwi.properties";

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

        final var backlogKey = keyRegistry.getKey(SERVER_BACKLOG);
        final var backlogProperty = getProperty(sources, backlogKey);
        builder.backlog(backlogKey.valueParser().getInt(backlogProperty));

        final var portKey = keyRegistry.getKey(SERVER_PORT);
        final var portProperty = getProperty(sources, portKey);
        builder.port(portKey.valueParser().getInt(portProperty));

        final var maxClientsKey = keyRegistry.getKey(SERVER_MAX_CLIENTS);
        final var maxClientsProperty = getProperty(sources, maxClientsKey);
        builder.maxClients(maxClientsKey.valueParser().getInt(maxClientsProperty));

        final var socketTimeoutKey = keyRegistry.getKey(SOCKET_TIMEOUT);
        final var socketTimeoutProperty = getProperty(sources, socketTimeoutKey);
        builder.soTimeoutMillis(socketTimeoutKey.valueParser().getInt(socketTimeoutProperty));

        final var metricsEnabledKey = keyRegistry.getKey(METRICS_ENABLED);
        final var metricsEnabledProperty = getProperty(sources, metricsEnabledKey);
        builder.metricsEnabled(metricsEnabledKey.valueParser().getBoolean(metricsEnabledProperty));

        final var config = builder.build();
        logConfig(config);
        return config;
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
                + METRICS_ENABLED + "=" + config.metricsEnabled());
    }
}
