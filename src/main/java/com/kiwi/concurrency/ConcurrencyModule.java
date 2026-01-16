package com.kiwi.concurrency;

import com.kiwi.observability.ObservabilityModule;

import static com.kiwi.concurrency.util.Constants.THREAD_NAME_PREFIX;

public final class ConcurrencyModule {
    private static KiwiThreadPool rejectionThreadPool;
    private static final String REJECTION_THREAD_POOL_NAME = "rejection-thread-pool";

    public static KiwiThreadPoolExecutor createExecutor(String threadPoolExecutorName,
                                                        String threadPoolName,
                                                        int threadPoolSize,
                                                        int queueCapacity) {
        final var metrics = ObservabilityModule.getThreadPoolMetrics(threadPoolName);
        final var threadFactory = new KiwiThreadFactory(threadPoolName);
        final var executor = new KiwiThreadPoolExecutor(
                threadPoolExecutorName,
                threadFactory,
                threadPoolName,
                threadPoolSize,
                queueCapacity,
                metrics,
                getRejectionThreadPool());
        metrics.registerMetrics();
        return executor;
    }

    private synchronized static KiwiThreadPool getRejectionThreadPool() {
        if (rejectionThreadPool == null) {
            final var kiwiThreadFactory = new KiwiThreadFactory(THREAD_NAME_PREFIX);
            final var metrics = ObservabilityModule.getThreadPoolMetrics(REJECTION_THREAD_POOL_NAME);
            metrics.registerMetrics();
            final var rejectionTp =  new KiwiThreadPool(kiwiThreadFactory, REJECTION_THREAD_POOL_NAME,
                    1, 100, metrics);
            rejectionTp.start();
            rejectionThreadPool = rejectionTp;
        }

        return rejectionThreadPool;
    }
}
