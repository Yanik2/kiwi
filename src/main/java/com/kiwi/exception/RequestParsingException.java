package com.kiwi.exception;

public class RequestParsingException extends RuntimeException {
    public RequestParsingException(String message, Exception ex) {
        super(message, ex);
    }
}
