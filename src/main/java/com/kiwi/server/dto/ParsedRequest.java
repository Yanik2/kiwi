package com.kiwi.server.dto;

import com.kiwi.server.Method;

import java.util.UUID;

public final class ParsedRequest extends TCPRequest {
    private final byte[] key;
    private final byte[] value;

    public ParsedRequest(UUID requestId, int flags, Method method, byte[] key, byte[] value) {
        super(requestId, flags, method);
        this.key = key;
        this.value = value;
    }

    public ParsedRequest(UUID requestId, int flags, Method method) {
        this(requestId, flags, method, null, null);
    }

    public byte[] getKey() {
        return this.key;
    }

    public byte[] getValue() {
        return this.value;
    }
}
