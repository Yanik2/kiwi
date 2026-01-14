package com.kiwi.concurrency;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class KiwiThreadFactory implements ThreadFactory {
    private final AtomicInteger threadNumber = new AtomicInteger(1);
    private final String namePrefix;

    public KiwiThreadFactory(String namePrefix) {
        this.namePrefix = namePrefix;
    }

    @Override
    public Thread newThread(Runnable r) {
        final var thread = new Thread(Thread.currentThread().getThreadGroup(), r,
                namePrefix + threadNumber.getAndIncrement());
        thread.setUncaughtExceptionHandler(KiwiThreadUncaughtExceptionHandler.getInstance());
        return thread;
    }
}
