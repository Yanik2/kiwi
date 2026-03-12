package com.kiwi.concurrency.factory;

import com.kiwi.concurrency.KiwiThreadFactory;
import com.kiwi.concurrency.KiwiThreadPool;
import com.kiwi.concurrency.KiwiThreadPoolExecutor;
import com.kiwi.observability.factory.ObservabilityContainer;
import com.kiwi.observability.ThreadPoolMetrics;

import java.util.Map;

import static com.kiwi.config.properties.Properties.REJECTION_POOL_SIZE;
import static com.kiwi.config.properties.Properties.REJECTION_QUEUE_SIZE;
import static com.kiwi.config.properties.Properties.REJECTION_THREAD_POOL_NAME;
import static com.kiwi.config.properties.Properties.SERVER_THREAD_POOL_EXECUTOR_NAME;
import static com.kiwi.config.properties.Properties.SERVER_THREAD_POOL_NAME;
import static com.kiwi.config.properties.Properties.SERVER_THREAD_POOL_QUEUE_CAP;
import static com.kiwi.config.properties.Properties.SERVER_THREAD_POOL_SIZE;
import static com.kiwi.config.properties.Properties.THREAD_NAME_PREFIX;

public final class ConcurrencyModule {
    // ignore IDE warning, will be used in other executors
    private static KiwiThreadPool rejectionThreadPool;

    public static ConcurrencyContainer create(ObservabilityContainer observabilityContainer) {
        final var rejectionThreadFactory = new KiwiThreadFactory(THREAD_NAME_PREFIX);
        final var metrics = observabilityContainer.threadPoolMetrics().get(REJECTION_THREAD_POOL_NAME);
        rejectionThreadPool = new KiwiThreadPool(rejectionThreadFactory, REJECTION_THREAD_POOL_NAME,
                REJECTION_POOL_SIZE, REJECTION_QUEUE_SIZE, metrics);
        return new ConcurrencyContainer(
                Map.of(SERVER_THREAD_POOL_EXECUTOR_NAME,
                createExecutor(
                        SERVER_THREAD_POOL_EXECUTOR_NAME,
                        SERVER_THREAD_POOL_NAME,
                        SERVER_THREAD_POOL_SIZE,
                        SERVER_THREAD_POOL_QUEUE_CAP,
                        observabilityContainer.threadPoolMetrics().get(SERVER_THREAD_POOL_NAME),
                        rejectionThreadPool)
                )
        );
    }

    // ignore IDE warnings, there will be another executors
    private static KiwiThreadPoolExecutor createExecutor(String threadPoolExecutorName,
                                                        String threadPoolName,
                                                        int threadPoolSize,
                                                        int queueCapacity,
                                                        ThreadPoolMetrics metrics,
                                                         KiwiThreadPool rejectionThreadPool) {
        final var threadFactory = new KiwiThreadFactory(threadPoolName);
        return new KiwiThreadPoolExecutor(
                threadPoolExecutorName,
                threadFactory,
                threadPoolName,
                threadPoolSize,
                queueCapacity,
                metrics,
                rejectionThreadPool);
    }
}
