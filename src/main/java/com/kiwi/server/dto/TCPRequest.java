package com.kiwi.server.dto;

import com.kiwi.server.Method;

public abstract class TCPRequest {
    private final int flags;
    private final Method method;

    protected TCPRequest(int flags, Method method) {
        this.flags = flags;
        this.method = method;
    }

    public Method getMethod() {
        return this.method;
    }

    public int getFlags() {
        return this.flags;
    }
}
