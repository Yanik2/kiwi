package com.kiwi.server.response.model;

public enum BinaryResponseValues {
    SUCCESS(() -> new byte[]{1}),
    FAIL(() -> new byte[]{0});

    private SerializableValue val;

    BinaryResponseValues(SerializableValue val) {
        this.val = val;
    }

    public SerializableValue getValue() {
        return this.val;
    }
}
