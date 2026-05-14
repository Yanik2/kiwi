package com.kiwi.config.provider;

import com.kiwi.config.domain.Config;

public class ConfigurationHolder {
    private final Config config;

    public ConfigurationHolder(Config config) {
        this.config = config;
    }

    public Config getConfig() {
        return this.config;
    }
}
