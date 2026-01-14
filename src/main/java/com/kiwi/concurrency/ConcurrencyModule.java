package com.kiwi.concurrency;

import com.kiwi.observability.ObservabilityModule;

import static com.kiwi.concurrency.util.Constants.THREAD_NAME_PREFIX;

public final class ConcurrencyModule {
    private static KiwiThreadPool rejectionThreadPool;
    private static final String REJECTION_THREAD_POOL_NAME = "rejection-thread-pool";
    private static final Object SYNC_OBJECT = new Object();

    public static KiwiThreadPoolExecutor createExecutor(String threadPoolExecutorName,
                                                        String threadPoolName,
                                                        int threadPoolSize,
                                                        int queueCapacity) {
        final var metrics = ObservabilityModule.getThreadPoolMetrics(threadPoolName);
        final var threadFactory = new KiwiThreadFactory(THREAD_NAME_PREFIX);
        final var executor = new KiwiThreadPoolExecutor(
                threadPoolExecutorName,
                threadFactory,
                threadPoolName,
                threadPoolSize,
                queueCapacity,
                metrics,
                rejectionThreadPool);
        metrics.registerMetrics();
        return executor;
    }

    public static void init() {
        if (rejectionThreadPool == null) {
            synchronized (SYNC_OBJECT) {
                if (rejectionThreadPool == null) {
                    final var kiwiThreadFactory = new KiwiThreadFactory(THREAD_NAME_PREFIX);
                    final var metrics = ObservabilityModule.getThreadPoolMetrics(REJECTION_THREAD_POOL_NAME);
                    rejectionThreadPool = new KiwiThreadPool(kiwiThreadFactory, REJECTION_THREAD_POOL_NAME,
                            1, 100, metrics);
                    metrics.registerMetrics();
                    rejectionThreadPool.start();
                }
            }
        }
    }
}
