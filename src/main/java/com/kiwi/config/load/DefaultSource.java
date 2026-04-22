package com.kiwi.config.load;

import com.kiwi.config.registry.ConfigKey;

public class DefaultSource implements PropertySource {

    @Override
    public String load(ConfigKey configKey) {
        return configKey.defaultValue();
    }
}
