package com.kiwi.config.load;

import com.kiwi.config.registry.ConfigKey;

import static com.kiwi.config.util.ConfigConstants.PROPERTY_PREFIX;

public class SystemPropertiesSource implements PropertySource {

    @Override
    public String load(ConfigKey configKey) {
        return System.getProperty(PROPERTY_PREFIX + configKey.name());
    }
}
