package com.kiwi.server.dto;

import com.kiwi.server.Method;

import java.util.UUID;

public abstract class TCPRequest {
    private final UUID requestId;
    private final int flags;
    private final Method method;

    protected TCPRequest(UUID requestId, int flags, Method method) {
        this.requestId = requestId;
        this.flags = flags;
        this.method = method;
    }

    public Method getMethod() {
        return this.method;
    }

    public int getFlags() {
        return this.flags;
    }

    public UUID getRequestId() {
        return this.requestId;
    }
}
