package com.kiwi.persistent.model;

import java.util.Arrays;

public final class Key {
    private final byte[] key;

    private int hashcode;

    public Key(byte[] key) {
        this.key = key;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Key key1 = (Key) o;
        return Arrays.equals(key, key1.key);
    }

    @Override
    public int hashCode() {
        if (hashcode == 0) {
            hashcode = Arrays.hashCode(key);
        }
        return hashcode;
    }
}
