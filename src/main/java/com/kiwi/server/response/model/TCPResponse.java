package com.kiwi.server.response.model;

import com.kiwi.server.request.Method;

import java.util.UUID;

public record TCPResponse(
        int requestId,
        Method method,
        UUID connectionId,
        SerializableValue responsePayload,
        String message,
        boolean isSuccess
) {

    public TCPResponse(int requestId, Method method, String message, boolean isSuccess, UUID connectionId) {
        this(requestId, method, connectionId, () -> new byte[0], message, isSuccess);
    }

    public TCPResponse(int requestId, String message, boolean isSuccess, UUID connectionId) {
        this(requestId, null, connectionId, () -> new byte[0], message, isSuccess);
    }
}
