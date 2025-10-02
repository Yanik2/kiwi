package com.kiwi.server.response;

@FunctionalInterface
public interface SerializableValue {
    byte[] serialize();
}
