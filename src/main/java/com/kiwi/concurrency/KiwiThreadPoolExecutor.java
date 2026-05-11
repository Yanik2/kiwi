package com.kiwi.concurrency;

import com.kiwi.concurrency.task.Task;
import com.kiwi.log.KiwiLogger;
import com.kiwi.log.KiwiLoggerFactory;
import com.kiwi.observability.metrics.ThreadPoolMetrics;

import java.util.concurrent.ThreadFactory;

public class KiwiThreadPoolExecutor {
    private static final KiwiLogger logger = KiwiLoggerFactory.getLogger(KiwiThreadPoolExecutor.class.getName());

    private final KiwiThreadPool executionThreadPool;
    private final KiwiThreadPool rejectionThreadPool;
    private final String name;

    public KiwiThreadPoolExecutor(String name,
                                  ThreadFactory threadFactory,
                                  String threadPoolName,
                                  int threadPoolSize,
                                  int queueCapacity,
                                  ThreadPoolMetrics threadPoolMetrics,
                                  KiwiThreadPool rejectionThreadPool) {
        this.executionThreadPool = new KiwiThreadPool(threadFactory, threadPoolName, threadPoolSize, queueCapacity,
                threadPoolMetrics);
        this.rejectionThreadPool = rejectionThreadPool;
        this.name = name;
    }

    public void submit(Task task) {
        final var taskSubmitted = executionThreadPool.submit(task::execute, task.getTimeout());
        if (!taskSubmitted) {
            final var rejected = rejectionThreadPool.submit(task::reject, 0);
            if (!rejected) {
                logger.info("Task cannot be processed, thread pool executor: [" + this.name + "], " +
                        "task will be rejected in caller thread");
                task.reject();
            }
        }
    }

    public void start() {
        executionThreadPool.start();
        rejectionThreadPool.start();
    }

    public double getLoadFactor() {
        return this.executionThreadPool.getLoadFactor();
    }

    public void shutdown() {
        try {
            executionThreadPool.stop();
            rejectionThreadPool.stop();
        } catch (Exception ex) {
            logger.warn("Exception during stop thread pools", ex.getMessage());
        }
    }
}
