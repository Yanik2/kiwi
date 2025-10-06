package com.kiwi.exception.protocol;

public class ProtocolException extends RuntimeException {
    private final ProtocolErrorCode protocolErrorCode;

    public ProtocolException(String message, ProtocolErrorCode protocolErrorCode) {
        super(message);
        this.protocolErrorCode = protocolErrorCode;
    }

    public ProtocolErrorCode getProtocolErrorCode() {
        return protocolErrorCode;
    }
}
