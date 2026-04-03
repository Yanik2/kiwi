package com.kiwi.server.request.model;

import com.kiwi.server.request.Method;

public final class NumericRequest extends TCPRequest {
    private final byte[] key;
    private final long value;

    public NumericRequest(int requestId, int flags, byte[] key, long value, Method method) {
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
