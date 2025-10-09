package com.kiwi.persistent.dto;

import com.kiwi.persistent.model.Key;
import com.kiwi.persistent.model.Value;

public record StorageRequest(
    Key key,
    Value value,
    long expiration
) {
    public StorageRequest(Key key) {
        this(key, null, 0L);
    }

    public StorageRequest(Key key, long expiration) {
        this(key, null, expiration);
    }
}
