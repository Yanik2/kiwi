package com.kiwi.server.response.dto;

public record WriteResponseResult(
    int writtenBytes,
    WriteResponseStatus status
) {
}
