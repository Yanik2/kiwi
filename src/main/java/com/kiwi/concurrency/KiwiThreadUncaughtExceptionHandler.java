package com.kiwi.concurrency;

import java.util.logging.Logger;

public final class KiwiThreadUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {
    private static final Logger logger = Logger.getLogger(KiwiThreadUncaughtExceptionHandler.class.getName());
    private static final KiwiThreadUncaughtExceptionHandler instance = new KiwiThreadUncaughtExceptionHandler();
    private static final String LOGGER_MESSAGE = "Thread [%s] uncaught exception: %s";

    private KiwiThreadUncaughtExceptionHandler() {}

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        //TODO handle exception, for now it's just a logger
        logger.severe(LOGGER_MESSAGE.formatted(t.getName(), e.getMessage()));
    }

    public static KiwiThreadUncaughtExceptionHandler getInstance() {
        return instance;
    }
}
