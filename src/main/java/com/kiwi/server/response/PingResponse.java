package com.kiwi.server.response;

public class PingResponse implements SerializableValue {
    @Override
    public byte[] serialize() {
        return new byte[]{80, 79, 78, 71};
    }
}
