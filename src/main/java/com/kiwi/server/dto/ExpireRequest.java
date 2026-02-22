package com.kiwi.server.dto;

import com.kiwi.server.Method;

import java.util.UUID;

public final class ExpireRequest extends TCPRequest {
    private final byte[] key;
    private final long value;

    public ExpireRequest(UUID requestId, int flags, Method method, byte[] key, long value) {
        super(requestId, flags, method);
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
