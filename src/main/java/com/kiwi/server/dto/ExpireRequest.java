package com.kiwi.server.dto;

import com.kiwi.server.Method;

public final class ExpireRequest extends TCPRequest {
    private final byte[] key;
    private final long value;

    public ExpireRequest(Method method, byte[] key, long value) {
        super(method);
        this.key = key;
        this.value = value;
    }

    public byte[] getKey() {
        return key;
    }

    public long getValue() {
        return value;
    }
}
