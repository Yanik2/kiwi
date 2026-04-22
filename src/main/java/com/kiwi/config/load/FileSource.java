package com.kiwi.config.load;

import com.kiwi.config.registry.ConfigKey;
import java.util.Properties;

public class FileSource implements PropertySource {
    private final Properties properties;

    public FileSource(Properties props) {
        this.properties = props;
    }

    @Override
    public String load(ConfigKey configKey) {
        return properties.getProperty(configKey.name());
    }
}
