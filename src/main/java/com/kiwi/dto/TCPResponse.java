package com.kiwi.dto;

public record TCPResponse(
    byte[] value,
    String message
) {
    public TCPResponse(String message) {
        this(null, message);
    }
}
