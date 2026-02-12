package com.kiwi.server.dto;

import com.kiwi.server.Method;

public final class ParsedRequest extends TCPRequest {
    private final byte[] key;
    private final byte[] value;

    public ParsedRequest(int flags, Method method, byte[] key, byte[] value) {
        super(flags, method);
        this.key = key;
        this.value = value;
    }

    public ParsedRequest(int flags, Method method, byte[] key) {
        this(flags, method, key, null);
    }

    public ParsedRequest(int flags, Method method) {
        this(flags, method, null, null);
    }

    public byte[] getKey() {
        return this.key;
    }

    public byte[] getValue() {
        return this.value;
    }
}
