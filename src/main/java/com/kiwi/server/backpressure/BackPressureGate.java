package com.kiwi.server.backpressure;

import com.kiwi.concurrency.KiwiThreadPoolExecutor;
import com.kiwi.observability.metrics.ThreadPoolMetrics;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static com.kiwi.config.properties.Properties.BP_HIGH_LOAD_WATERMARK;
import static com.kiwi.config.properties.Properties.BP_LOW_LOAD_WATERMARK;
import static java.util.concurrent.TimeUnit.SECONDS;

public class BackPressureGate {

    private final Lock lock = new ReentrantLock();
    private final Condition overloaded;
    private final KiwiThreadPoolExecutor threadPoolExecutor;
    private final ThreadPoolMetrics tpMetrics;

    private volatile boolean closed;

    public BackPressureGate(KiwiThreadPoolExecutor threadPoolExecutor, ThreadPoolMetrics tpMetrics) {
        this.threadPoolExecutor = threadPoolExecutor;
        this.tpMetrics = tpMetrics;
        this.overloaded = lock.newCondition();
    }

    public void awaitIfOverloaded() throws InterruptedException {
        lock.lock();
        try {
            if (this.threadPoolExecutor.getLoadFactor() >= BP_HIGH_LOAD_WATERMARK) {
                this.closed = true;
            }
            while (closed) {
                tpMetrics.onBpPaused(1);
                tpMetrics.onBpPauses();
                overloaded.await(10, SECONDS);
                tpMetrics.onBpPaused(-1);
            }
        } catch (InterruptedException ex) {
            tpMetrics.onBpPaused(-1);
            throw ex;
        } finally {
            lock.unlock();
        }
    }

    public void signalIfBelowLow() {
        lock.lock();
        if (closed && this.threadPoolExecutor.getLoadFactor() <= BP_LOW_LOAD_WATERMARK) {
            this.closed = false;
            overloaded.signalAll();
        }
        lock.unlock();
    }
}
