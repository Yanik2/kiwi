package com.kiwi.dto;

public record TCPRequest(
    String method,
    String key,
    byte[] value
) {
    public TCPRequest(String method, String key) {
        this(method, key, null);
    }

    public TCPRequest(String method) {
        this(method, null);
    }
}
