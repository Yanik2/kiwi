package com.kiwi.server.request.model;

import com.kiwi.jvm.jfr.event.KiwiRequest;
import com.kiwi.server.request.Method;

public abstract class TCPRequest {
    private final int requestId;
    private final int flags;
    private final Method method;
    private final KiwiRequest kiwiRequest;

    protected TCPRequest(int requestId, int flags, Method method, KiwiRequest request) {
        this.requestId = requestId;
        this.flags = flags;
        this.method = method;
        this.kiwiRequest = request;
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

    public KiwiRequest getKiwiRequest() {
        return kiwiRequest;
    }
}
