package com.kiwi.persistent.model;

import com.kiwi.persistent.model.expiration.ExpiryPolicy;
import java.util.Arrays;

public class Value {
    private final byte[] value;
    private final ExpiryPolicy expiryPolicy;

    public Value(byte[] value, ExpiryPolicy expiryPolicy) {
        this.value = value;
        this.expiryPolicy = expiryPolicy;
    }

    public byte[] getValue() {
        return value;
    }

    public ExpiryPolicy getExpiryPolicy() {
        return expiryPolicy;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Value value1 = (Value) o;
        return Arrays.equals(value, value1.value);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(value);
    }
}
