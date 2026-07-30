package com.kiwi.server.response.model;

import com.kiwi.jvm.jfr.event.KiwiRequest;
import com.kiwi.server.request.Method;

public record TCPResponse(
        int requestId,
        KiwiRequest kiwiRequest,
        Method method,
        long connectionId,
        SerializableValue responsePayload,
        String message,
        boolean isSuccess,
        TCPResponseResult result
) {

    public TCPResponse(int requestId, KiwiRequest kiwiRequest, Method method, String message, boolean isSuccess,
                       long connectionId, TCPResponseResult result) {
        this(requestId, kiwiRequest, method, connectionId, () -> new byte[0], message, isSuccess, result);
    }

    public TCPResponse(int requestId, String message, boolean isSuccess, long connectionId, TCPResponseResult result) {
        this(requestId, null, null, connectionId, () -> new byte[0], message, isSuccess, result);
    }

    public void completeRequestExecution() {
        if (kiwiRequest != null) {
            kiwiRequest.waitForComplete();
        }
    }

    public void completeRequest() {
        if (kiwiRequest != null) {
            kiwiRequest.complete(requestId, method, connectionId, result);
        }
    }
}
