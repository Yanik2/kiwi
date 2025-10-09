package com.kiwi.server;

import java.util.Set;

public enum Method {
    GET,
    SET,
    DEL,
    EXT,
    INF,
    PING,
    EXPIRE,
    PEXPIRE,
    PERSIST;

    private static final Set<Method> keyLessMethods = Set.of(EXT, INF, PING);

    public boolean isKeyless() {
        return keyLessMethods.contains(this);
    }
}
