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

import static com.kiwi.config.util.ConfigConstants.METRICS_ENABLED;
import static com.kiwi.config.util.ConfigConstants.SERVER_BACKLOG;
import static com.kiwi.config.util.ConfigConstants.SERVER_MAX_CLIENTS;
import static com.kiwi.config.util.ConfigConstants.SERVER_PORT;
import static com.kiwi.config.util.ConfigConstants.SOCKET_TIMEOUT;

public class ConfigModule {
    private static final String DEFAULT_FILE_PATH = "config/kiwi.properties";
    private static final String FILE_KEY = "-Dkiwi.config";

    public static Config createConfig(String[] args) {

        var filePath = DEFAULT_FILE_PATH;
        for (String arg : args) {
            if (arg.startsWith(FILE_KEY)) {
                filePath = arg.substring(arg.indexOf("=") + 1);
            }
        }

        final var props = ConfigurationFileLoader.loadProperties(filePath);
        final var fileSource = new FileSource(props);
        final var defaultSource = new DefaultSource();
        final var envSource = new EnvSource();
        final var systemSource = new SystemPropertiesSource();
        final var sources = List.of(systemSource, envSource, fileSource, defaultSource);
        final var keyRegistry = PropertiesKeyRegistry.getInstance();

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

        return builder.build();

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
}
