package com.kiwi.config.registry;

import com.kiwi.exception.config.ConfigurationInitializationException;

public class ValueParser {

    public int getInt(String value, String message) {
        try {
            return Integer.parseInt(value);
        } catch (Exception ex) {
            throw new ConfigurationInitializationException(
                    message + value, ex);
        }
    }

    public boolean getBoolean(String value, String message) {
            if (!Boolean.TRUE.toString().equalsIgnoreCase(value)
                    && !Boolean.FALSE.toString().equalsIgnoreCase(value)) {
                throw new ConfigurationInitializationException(message + value);
            }

            return Boolean.parseBoolean(value);
    }

}
