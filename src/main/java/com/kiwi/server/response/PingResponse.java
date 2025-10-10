package com.kiwi.server.response;

public class PingResponse implements SerializableValue {
    private static final PingResponse instance  = new PingResponse();

    private final byte[] pongBytes = new byte[]{80, 79, 78, 71};

    public static SerializableValue getInstance() {
        return instance;
    }
    @Override
    public byte[] serialize() {
        return pongBytes;
    }
}
