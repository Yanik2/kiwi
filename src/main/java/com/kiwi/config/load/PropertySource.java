package com.kiwi.config.load;

import com.kiwi.config.registry.ConfigKey;

public interface PropertySource {
    String load(ConfigKey configKey);
}
