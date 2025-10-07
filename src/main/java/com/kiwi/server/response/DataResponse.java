package com.kiwi.server.response;

import com.kiwi.persistent.model.Value;

public record DataResponse(
    Value value
) implements SerializableValue {

    @Override
    public byte[] serialize() {
        return value.getValue();
    }
}
