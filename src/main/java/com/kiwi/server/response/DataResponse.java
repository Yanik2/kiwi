package com.kiwi.server.response;

import com.kiwi.persistent.dto.Value;

public record DataResponse(
    Value value
) implements SerializableValue {

    @Override
    public byte[] serialize() {
        //TODO clarify on absent value
        return value == null ? new byte[0] : value.getValue();
    }
}
