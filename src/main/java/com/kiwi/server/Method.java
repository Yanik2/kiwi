package com.kiwi.server;

import java.util.Set;

public enum Method {
    GET, SET, DEL, EXT, INF, UNKNOWN;

    private static final Set<Method> keyLessMethods = Set.of(EXT, INF, UNKNOWN);

    public boolean isKeyless() {
        return keyLessMethods.contains(this);
    }
}
