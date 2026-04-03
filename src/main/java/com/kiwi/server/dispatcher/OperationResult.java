package com.kiwi.server.dispatcher;

import com.kiwi.server.response.model.SerializableValue;

public record OperationResult(
        SerializableValue value,
        boolean success
) {
}
