package com.kiwi.exception.config;

public class ConfigurationValidationException extends RuntimeException {
    public ConfigurationValidationException(String message) {
        super(message);
    }
}
