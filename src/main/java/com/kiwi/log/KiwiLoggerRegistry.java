package com.kiwi.log;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class KiwiLoggerRegistry {
    private static final KiwiLoggerRegistry instance = new KiwiLoggerRegistry();

    private final ConcurrentMap<String, KiwiLogger> loggers = new ConcurrentHashMap<>();

    private KiwiLoggerRegistry() {}

    public static KiwiLoggerRegistry getInstance() {
        return instance;
    }

    public KiwiLogger addLogger(String loggerName, KiwiLogger logger) {
        return loggers.computeIfAbsent(loggerName, k -> logger);
    }
}
