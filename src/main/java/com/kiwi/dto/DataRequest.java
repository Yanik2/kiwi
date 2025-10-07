package com.kiwi.dto;

public record DataRequest(
    byte[] key,
    byte[] value
) {
    public DataRequest(byte[] key) {
        this(key, null);
    }
}
