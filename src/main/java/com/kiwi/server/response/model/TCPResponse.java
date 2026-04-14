package com.kiwi.server.response.model;

public record TCPResponse(
        int requestId,
        SerializableValue responsePayload,
        String message,
        boolean isSuccess
) {

    public TCPResponse(int requestId, String message, boolean isSuccess) {
        this(requestId, () -> new byte[0], message, isSuccess);
    }
}
