package com.kiwi.concurrency;

import com.kiwi.log.KiwiLogger;
import com.kiwi.log.KiwiLoggerFactory;

public final class KiwiThreadUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {
    private static final KiwiLogger log = KiwiLoggerFactory.getLogger(KiwiThreadUncaughtExceptionHandler.class.getName());

    private static final KiwiThreadUncaughtExceptionHandler instance = new KiwiThreadUncaughtExceptionHandler();

    private KiwiThreadUncaughtExceptionHandler() {}

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        //TODO handle exception, for now it's just a logger
        log.error("Error in thread: " + t.getName(), e.getMessage());
    }

    public static KiwiThreadUncaughtExceptionHandler getInstance() {
        return instance;
    }
}
