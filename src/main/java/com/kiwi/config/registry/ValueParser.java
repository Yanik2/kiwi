package com.kiwi.config.registry;

import com.kiwi.exception.config.ConfigurationInitializationException;

public interface ValueParser {
    default int getInt(String value) {
        throw new ConfigurationInitializationException("Value is not integer");
    }

    default boolean getBoolean(String value) {
        throw new ConfigurationInitializationException("Value is not boolean");
    }

    default String getString(String value) {
        throw new ConfigurationInitializationException("Value is not string");
    }
}
