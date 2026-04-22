package com.kiwi.config.load;

import com.kiwi.config.registry.ConfigKey;

public class EnvSource implements PropertySource {
    @Override
    public String load(ConfigKey configKey) {
        return System.getenv(configKey.envName());
    }
}
