package com.kiwi.server.dto;

import com.kiwi.server.Method;

public final class ExpireRequest extends TCPRequest {
    private final byte[] key;
    private final long value;

    public ExpireRequest(int flags, Method method, byte[] key, long value) {
        super(flags, method);
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
