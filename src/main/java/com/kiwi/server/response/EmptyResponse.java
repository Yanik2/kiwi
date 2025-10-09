package com.kiwi.server.response;

public final class EmptyResponse implements SerializableValue {
    private static final EmptyResponse instance = new EmptyResponse();

    private final byte[] emptyValue = new byte[0];

    private EmptyResponse() {}

    public static SerializableValue getInstance() {
        return instance;
    }

    @Override
    public byte[] serialize() {
        return emptyValue;
    }
}
