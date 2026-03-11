package com.kiwi.server.request.model;

import com.kiwi.server.request.Method;

public final class ParsedRequest extends TCPRequest {
    private final byte[] key;
    private final byte[] value;

    public ParsedRequest(int requestId, int flags, Method method, byte[] key, byte[] value) {
        super(requestId, flags, method);
        this.key = key;
        this.value = value;
    }

    public ParsedRequest(int requestId, int flags, Method method) {
        this(requestId, flags, method, null, null);
    }

    public byte[] getKey() {
        return this.key;
    }

    public byte[] getValue() {
        return this.value;
    }
}
