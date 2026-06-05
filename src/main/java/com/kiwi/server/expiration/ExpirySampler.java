package com.kiwi.server.expiration;

import com.kiwi.config.util.EvictionPolicy;
import com.kiwi.exception.IllegalStateException;
import com.kiwi.log.KiwiLogger;
import com.kiwi.log.KiwiLoggerFactory;

public class ExpirySampler {
    private static final KiwiLogger log = KiwiLoggerFactory.getLogger(ExpirySampler.class.getName());

    private final int period;
    private final int batch;
    private final int backoff;
    private final int maxBytes;
    private final EvictionPolicy policy;

    private Thread thread;
    private volatile boolean running = false;

    public ExpirySampler(int period, int batch, int backoff, int maxBytes, EvictionPolicy policy) {
        this.period = period;
        this.batch = batch;
        this.backoff = backoff;
        this.maxBytes = maxBytes;
        this.policy = policy;
        this.thread = new Thread(sample());
    }

    public void start() {
        if (thread.isAlive()) {
            log.error("Cannot start sampler thread", "Thread has already started");
            throw new IllegalStateException("Trying to start running sampler thread");
        }

        thread.start();
        running = true;
    }

    public void shutdown() {
        this.running = false;
        thread.interrupt();
        try {
            thread.join(10000);
        } catch (InterruptedException e) {
            log.warn("Unexpected exception during sampler shutdown");
            // ignore
        }
    }

    private Runnable sample() {
        return () -> {
            while (running) {
                try {
                    final var result = performCycle();
                    Thread.sleep(result.nextSleepTime);
                } catch (Exception e) {
                    log.error("Sampler thread was interrupted", e.getMessage());
                }
            }
        };
    }

    // TODO will return performance result, that contains next sleep period, and other data of clean up
    private CycleResult performCycle() {
        // temporary stub
        return new CycleResult(0);
    }

    private record CycleResult(
            int nextSleepTime
            // will have more field on logic implementation
    ) {}
}
