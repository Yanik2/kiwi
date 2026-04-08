package com.kiwi.server.request.model;

import com.kiwi.server.request.Method;

import java.util.Collections;
import java.util.List;

public final class ParsedRequest extends TCPRequest {
    private final List<KeyValuePair> keyValuePairs;

    public ParsedRequest(int requestId,
                         int flags,
                         Method method,
                         List<KeyValuePair> keyValuePairs) {
        super(requestId, flags, method);
        this.keyValuePairs = keyValuePairs;
    }

    public ParsedRequest(int requestId, int flags, Method method) {
        this(requestId, flags, method, null);
    }

    public byte[] getKey() {
        return this.keyValuePairs.getFirst().key;
    }

    public byte[] getValue() {
        return this.keyValuePairs.getFirst().value;
    }

    public List<byte[]> getKeys() {
        return keyValuePairs.stream()
                .map(p -> p.key)
                .toList();
    }

    public List<KeyValuePair> getKeyValues() {
        return Collections.unmodifiableList(keyValuePairs);
    }

    public int size() {
        return keyValuePairs == null ? 0 : keyValuePairs.size();
    }

    public static class KeyValuePair {
        private final byte[] key;
        private final byte[] value;

        public KeyValuePair(byte[] key, byte[] value) {
            this.key = key;
            this.value = value;
        }

        public byte[] getKey() {
            return key;
        }

        public byte[] getValue() {
            return value;
        }
    }
}
