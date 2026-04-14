package com.kiwi.server.request.model;

import com.kiwi.server.request.Method;

public abstract class TCPRequest {
    private final int requestId;
    private final int flags;
    private final Method method;
    private final long start;

    protected TCPRequest(int requestId, int flags, Method method) {
        this.requestId = requestId;
        this.flags = flags;
        this.method = method;
        this.start = System.currentTimeMillis();
    }

    public Method getMethod() {
        return this.method;
    }

    public int getFlags() {
        return this.flags;
    }

    public int getRequestId() {
        return this.requestId;
    }

    public long getStart() {
        return this.start;
    }
}
