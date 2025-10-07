package com.kiwi.server.dto;

import com.kiwi.server.Method;

public record TCPRequest(
    Method method,
    byte[] key,
    byte[] value
) {
    public TCPRequest(Method method, byte[] key) {
        this(method, key, null);
    }

    public TCPRequest(Method method) {
        this(method, null, null);
    }
}
