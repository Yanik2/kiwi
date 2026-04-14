package com.kiwi.server.response.model;

import com.kiwi.server.context.ConnectionContext;

public record TCPResponse(
        int requestId,
        SerializableValue responsePayload,
        String message,
        boolean isSuccess,
        ConnectionContext context,
        long start
) {

    public TCPResponse(int requestId, String message, boolean isSuccess) {
        this(requestId, () -> new byte[0], message, isSuccess, null, 0L);
    }

    public TCPResponse(int requestId, String message, boolean isSuccess, ConnectionContext context, long start) {
        this(requestId, () -> new byte[0], message, isSuccess, context, start);
    }

    public void setTime(long time) {
        context.setTime(requestId, time - start);
    }
}
