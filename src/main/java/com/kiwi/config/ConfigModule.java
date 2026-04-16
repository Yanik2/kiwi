package com.kiwi.config;

import com.kiwi.config.builder.ConfigBuilder;
import com.kiwi.config.domain.Config;

public class ConfigModule {
    public static Config createConfig() {
        final var builder = new ConfigBuilder();
        return builder.build();
    }
}
