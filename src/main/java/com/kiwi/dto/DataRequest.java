package com.kiwi.dto;

public record DataRequest(
    String key,
    byte[] data
) {
}
