package com.kiwi.server.response.model;

import static com.kiwi.server.response.ResponseValueConstants.EMPTY_RESPONSE;

public final class EmptyResponse implements SerializableValue {
    private static final EmptyResponse instance = new EmptyResponse();

    private EmptyResponse() {}

    public static SerializableValue getInstance() {
        return instance;
    }

    @Override
    public byte[] serialize() {
        return EMPTY_RESPONSE;
    }
}
