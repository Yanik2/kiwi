package com.kiwi.server;

import java.util.Set;

public enum Method {
    GET, SET, DEL, EXT, INF;

    private static final Set<Method> keyLessMethods = Set.of(EXT, INF);

    public boolean isKeyless() {
        return keyLessMethods.contains(this);
    }
}
