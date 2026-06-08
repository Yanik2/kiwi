package com.kiwi.server.expiration;

import com.kiwi.config.util.EvictionPolicy;
import com.kiwi.exception.IllegalSamplerStateException;
import com.kiwi.log.KiwiLogger;
import com.kiwi.log.KiwiLoggerFactory;
import com.kiwi.observability.metrics.ExpirySampleMetrics;
import com.kiwi.persistent.model.Key;
import com.kiwi.persistent.storage.ExpirySamplingStorage;

public class ExpirySampler {
    private static final KiwiLogger log = KiwiLoggerFactory.getLogger(ExpirySampler.class.getName());

    private final ExpirySamplingStorage storage;
    private final ExpirySampleMetrics metrics;

    private final int period;
    private final int batch;
    private final int backoff;
    private final int maxBytes;
    private final EvictionPolicy policy;

    private Thread thread;
    private volatile boolean running = false;

    public ExpirySampler(ExpirySamplingStorage storage,
                         ExpirySampleMetrics metrics,
                         int period,
                         int batch,
                         int backoff,
                         int maxBytes,
                         EvictionPolicy policy) {
        this.metrics = metrics;
        this.storage = storage;
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
            throw new IllegalSamplerStateException("Trying to start running sampler thread");
        }

        this.running = true;
        thread.start();
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
                    metrics.onTtlScanned(result.scanned());
                    metrics.onActiveExpiredEvictions(result.expired);
                    Thread.sleep(result.nextSleepTime);
                } catch (Exception e) {
                    log.error("Sampler thread was interrupted", e.getMessage());
                    // TODO introduce states, process interrupting exception depending on states
                }
            }
        };
    }

    private CycleResult performCycle() {
        final var now = System.currentTimeMillis();
        final var keys = storage.sampleKeysWithTtl(batch);
        int evictionCounter = 0;

        for (Key key: keys) {
            if (storage.deleteIfExpired(key, now)) {
                evictionCounter++;
            }
        }

        return new CycleResult(period, keys.size(), evictionCounter);
    }

    private record CycleResult(
            int nextSleepTime,
            int scanned,
            int expired
    ) {}
}
