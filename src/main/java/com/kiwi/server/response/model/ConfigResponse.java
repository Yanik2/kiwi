package com.kiwi.server.response.model;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.stream.Collectors;

public class ConfigResponse implements SerializableValue {
    private final Map<String, Object> config;

    public ConfigResponse(Map<String, Object> config) {
        this.config = config;
    }

    @Override
    public byte[] serialize() {
        return config.entrySet()
                .stream()
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.joining(" "))
                .getBytes(StandardCharsets.UTF_8);
    }
}
