package com.kiwi.dto;

import com.kiwi.server.response.SerializableValue;

public record TCPResponse(
    SerializableValue responsePayload,
    String message,
    boolean isSuccess
) {
    public TCPResponse(String message) {
        this(() -> new byte[0], message, true);
    }

    public TCPResponse(String message, boolean isSuccess) {
        this(() -> new byte[0], message, isSuccess);
    }
}
