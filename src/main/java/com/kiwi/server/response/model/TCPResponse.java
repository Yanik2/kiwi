package com.kiwi.server.response.model;

import java.util.UUID;

public record TCPResponse(
        int requestId,
        UUID connectionId,
        SerializableValue responsePayload,
        String message,
        boolean isSuccess
) {

    public TCPResponse(int requestId, String message, boolean isSuccess, UUID connectionId) {
        this(requestId, connectionId, () -> new byte[0], message, isSuccess);
    }
}
