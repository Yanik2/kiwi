package com.kiwi.exception.jvm;

public class GcLogParsingException extends RuntimeException {
    public GcLogParsingException(String message, Throwable ex) {
        super(message, ex);
    }

    public GcLogParsingException(String message) {
        super(message);
    }
}
