package com.kiwi.exception;

public class UnknownMethodException extends RuntimeException {
    public UnknownMethodException(String message) {
        super(message);
    }
}
