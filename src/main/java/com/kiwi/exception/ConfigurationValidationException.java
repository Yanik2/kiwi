package com.kiwi.exception;

public class ConfigurationValidationException extends RuntimeException {
    public ConfigurationValidationException(String message) {
        super(message);
    }
}
