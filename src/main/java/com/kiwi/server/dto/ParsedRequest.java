package com.kiwi.server.dto;

import com.kiwi.server.Method;

public final class ParsedRequest extends TCPRequest {
    private final byte[] key;
    private final byte[] value;

    public ParsedRequest(Method method, byte[] key, byte[] value) {
        super(method);
        this.key = key;
        this.value = value;
    }

    public ParsedRequest(Method method, byte[] key) {
        this(method, key, null);
    }

    public ParsedRequest(Method method) {
        this(method, null, null);
    }

    public byte[] getKey() {
        return this.key;
    }

    public byte[] getValue() {
        return this.value;
    }
}
