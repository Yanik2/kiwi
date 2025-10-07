package com.kiwi.persistent.dto;

import com.kiwi.persistent.model.Key;
import com.kiwi.persistent.model.Value;

public record StorageRequest(
    Key key,
    Value value
) {
    public StorageRequest(Key key) {
        this(key, null);
    }
}
