package com.kiwi.concurrency;

import com.kiwi.concurrency.task.Task;
import com.kiwi.observability.ThreadPoolMetrics;

import java.util.concurrent.ThreadFactory;
import java.util.logging.Logger;

public class KiwiThreadPoolExecutor {
    private static final Logger logger = Logger.getLogger(KiwiThreadPoolExecutor.class.getName());

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
    }
}
