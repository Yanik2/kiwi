package com.kiwi.exception;

public class ResponseWritingException extends RuntimeException {
    public ResponseWritingException(String message) {
        super(message);
    }
}
