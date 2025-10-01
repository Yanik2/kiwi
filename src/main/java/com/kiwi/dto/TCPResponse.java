package com.kiwi.dto;

import com.kiwi.persistent.dto.Value;

public record TCPResponse(
    Value value,
    String message,
    boolean isSuccess
) {
    public TCPResponse(String message) {
        this(null, message, true);
    }

    public TCPResponse(String message, boolean isSuccess) {
        this(null, message, isSuccess);
    }
}
