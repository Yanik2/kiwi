package com.kiwi;


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
    GETSET,
    INCR,
    DECR,
    INCRBY,
    DECRBY,
    MGET,
    MSET,
    DBSIZE,
    QUIT;

    private static final Set<Method> keyLessMethods = Set.of(EXT, INF, PING, DBSIZE);
    private static final Set<Method> withValueMethods = Set.of(SET, EXPIRE, PEXPIRE, SETNX, GETSET, INCRBY, DECRBY, MSET);
    private static final Set<Method> multiKeyMethods = Set.of(MGET, MSET);

    public boolean isKeyless() {
        return keyLessMethods.contains(this);
    }

    public boolean withValue() {
        return withValueMethods.contains(this);
    }

    public boolean isMultiKey() {
        return multiKeyMethods.contains(this);
    }
}