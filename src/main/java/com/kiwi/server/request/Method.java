package com.kiwi.server.request;

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
    PERSIST,
    TTL,
    PTTL,
    EXISTS,
    SETNX,
    GETSET;

    private static final Set<Method> keyLessMethods = Set.of(EXT, INF, PING);
    private static final Set<Method> withValueMethods = Set.of(SET, EXPIRE, PEXPIRE, SETNX, GETSET);

    public boolean isKeyless() {
        return keyLessMethods.contains(this);
    }

    public boolean withValue() {
        return withValueMethods.contains(this);
    }
}
