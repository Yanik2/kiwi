package com.kiwi.config.domain;

import java.util.Set;

public enum JvmBuffersStrategy {
    HEAP, DIRECT;

    public static boolean exists(String strategy) {
        return stringStrategies.contains(strategy);
    }

    private static final Set<String> stringStrategies = Set.of(HEAP.name(), DIRECT.name());
}
