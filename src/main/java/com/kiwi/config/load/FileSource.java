package com.kiwi.config.load;

import com.kiwi.config.registry.ConfigKey;
import com.kiwi.config.registry.PropertiesKeyRegistry;

import java.util.Properties;
import java.util.logging.Logger;

import static java.util.logging.Level.WARNING;

public class FileSource implements PropertySource {
    private static final Logger log = Logger.getLogger(FileSource.class.getName());

    private final Properties properties;

    public FileSource(Properties props) {
        validateProperties(props);
        this.properties = props;
    }

    @Override
    public String load(ConfigKey configKey) {
        return properties.getProperty(configKey.name());
    }

    private void validateProperties(Properties props) {
        final var knownKeyNames = PropertiesKeyRegistry.getInstance().getKeyNames();
        for (Object pn : props.keySet()) {
            if (!knownKeyNames.contains(pn)) {
                log.log(WARNING, "Key name is not known: " + pn);
            }
        }
    }
}
