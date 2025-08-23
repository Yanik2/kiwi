package com.kiwi.dto;

import com.kiwi.persistent.dto.Value;

public record TCPResponse(
    Value value,
    String message
) {
    public TCPResponse(String message) {
        this(null, message);
    }
}
