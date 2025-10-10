package com.kiwi.server.dto;

import com.kiwi.server.Method;

public abstract class TCPRequest {
    private final Method method;

    protected TCPRequest(Method method) {
        this.method = method;
    }

    public Method getMethod() {
        return this.method;
    }
}
