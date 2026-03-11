package com.kiwi.server.request;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import static com.kiwi.config.properties.Properties.MAX_INFLIGHT_PER_CONNECTION;

public class RequestInflightLock {
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition condition = lock.newCondition();
    private final AtomicInteger inflight = new AtomicInteger();

    public void awaitInflightLevel() throws InterruptedException {
        lock.lock();
        try {
            while (inflight.get() >= MAX_INFLIGHT_PER_CONNECTION) {
                condition.await();
            }
        } finally {
            lock.unlock();
        }
    }

    public void notifyInflight() {
        lock.lock();
        try {
            if (inflight.get() < MAX_INFLIGHT_PER_CONNECTION) {
                condition.signalAll();

            }
        } finally {
            lock.unlock();
        }
    }

    public void onRequest() {
        inflight.incrementAndGet();
    }

    public void onResponse() {
        inflight.decrementAndGet();
    }
}
