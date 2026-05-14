package com.kiwi.config.util;

import java.util.Map;
import java.util.Set;

public enum EvictionPolicy {
    NO_EVICT("no_evict"), ALL_KEYS_LRU("allkeys-lru");

    private final String value;

    EvictionPolicy(String s) {
        this.value = s;
    }

    public String getValue() {
        return this.value;
    }

    public static boolean exists(String v) {
        return policies.containsKey(v);
    }

    public static EvictionPolicy get(String key) {
        return policies.get(key);
    }

    private static final Map<String, EvictionPolicy> policies =
            Map.of(NO_EVICT.getValue(), NO_EVICT, ALL_KEYS_LRU.getValue(), ALL_KEYS_LRU);
}
