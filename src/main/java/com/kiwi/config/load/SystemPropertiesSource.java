package com.kiwi.config.load;

import com.kiwi.config.registry.ConfigKey;

public class SystemPropertiesSource implements PropertySource {

    @Override
    public String load(ConfigKey configKey) {
        return System.getProperty(configKey.name());
    }
}
