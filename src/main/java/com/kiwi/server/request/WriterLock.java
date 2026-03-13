package com.kiwi.server.request;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import static com.kiwi.config.properties.Properties.MAX_INFLIGHT_PER_CONNECTION;

public class WriterLock {
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition inflightLevel = lock.newCondition();
    private final Condition writerDone = lock.newCondition();
    private final AtomicInteger inflight = new AtomicInteger();

    public void awaitWriterDone() throws InterruptedException {
        lock.lock();
        try {
            writerDone.await();
        } finally {
            lock.unlock();
        }
    }

    public void notifyWriterDone() {
        lock.lock();
        try {
            writerDone.signalAll();
        } finally {
            lock.unlock();
        }
    }

    public void awaitInflightLevel() throws InterruptedException {
        lock.lock();
        try {
            while (inflight.get() >= MAX_INFLIGHT_PER_CONNECTION) {
                inflightLevel.await();
            }
        } finally {
            lock.unlock();
        }
    }

    public void notifyInflight() {
        lock.lock();
        try {
            if (inflight.get() < MAX_INFLIGHT_PER_CONNECTION) {
                inflightLevel.signalAll();

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
