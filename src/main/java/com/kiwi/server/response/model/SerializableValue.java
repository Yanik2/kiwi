package com.kiwi.server.response.model;

@FunctionalInterface
public interface SerializableValue {
    byte[] serialize();
}
