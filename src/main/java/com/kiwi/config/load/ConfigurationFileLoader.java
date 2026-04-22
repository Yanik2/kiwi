package com.kiwi.config.load;

import com.kiwi.exception.config.ConfigurationInitializationException;

import java.io.FileInputStream;
import java.util.Properties;
import java.util.logging.Logger;

public class ConfigurationFileLoader {
    private static final Logger log = Logger.getLogger(ConfigurationFileLoader.class.getName());

    private static final String DEFAULT_FILE_PATH = "config/kiwi.properties";

    public static Properties loadProperties(String filePath) {
        if (DEFAULT_FILE_PATH.equals(filePath)) {
            return loadDefault();
        } else {
            return loadCustom(filePath);
        }
    }

    private static Properties loadDefault() {
        final var props = new Properties();

        try (final var fis = new FileInputStream(DEFAULT_FILE_PATH)) {
            props.load(fis);
        } catch (Exception ex) {
            log.info("Error during loading default configuration file: " + ex.getMessage());
        }

        return props;
    }

    private static Properties loadCustom(String filePath) {
        final var props = new Properties();

        try (final var fis = new FileInputStream(filePath)) {
            props.load(fis);
        } catch (Exception ex) {
            throw new ConfigurationInitializationException("Error during loading custom configuration file", ex);
        }

        return props;
    }
}
