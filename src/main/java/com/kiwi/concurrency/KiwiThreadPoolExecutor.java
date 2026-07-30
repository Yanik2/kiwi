package com.kiwi.concurrency;

import com.kiwi.concurrency.task.Task;
import com.kiwi.jvm.factory.JfrEventFactory;
import com.kiwi.jvm.jfr.event.KiwiExecutorQueue;
import com.kiwi.log.KiwiLogger;
import com.kiwi.log.KiwiLoggerFactory;
import com.kiwi.observability.metrics.ThreadPoolMetrics;

import java.util.concurrent.ThreadFactory;

public class KiwiThreadPoolExecutor {
    private static final KiwiLogger logger = KiwiLoggerFactory.getLogger(KiwiThreadPoolExecutor.class.getName());

    private final KiwiThreadPool executionThreadPool;
    private final KiwiThreadPool rejectionThreadPool;
    private final String name;
    private final JfrEventFactory jfrEventFactory;
    private final ThreadPoolMetrics threadPoolMetrics;

    private volatile boolean backpressureActive;

    public KiwiThreadPoolExecutor(String name,
                                  ThreadFactory threadFactory,
                                  String threadPoolName,
                                  int threadPoolSize,
                                  int queueCapacity,
                                  ThreadPoolMetrics threadPoolMetrics,
                                  KiwiThreadPool rejectionThreadPool,
                                  JfrEventFactory jfrEventFactory) {
        this.executionThreadPool = new KiwiThreadPool(threadFactory, threadPoolName, threadPoolSize, queueCapacity,
                threadPoolMetrics);
        this.rejectionThreadPool = rejectionThreadPool;
        this.name = name;
        this.jfrEventFactory = jfrEventFactory;
        this.threadPoolMetrics = threadPoolMetrics;
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
            final var queueLoad = jfrEventFactory.createEvent(KiwiExecutorQueue.class);
            queueLoad.onEvent(this.name, executionThreadPool.getQueueSize(), executionThreadPool.getQueueCap(),
                    executionThreadPool.getActiveWorkers(), executionThreadPool.getMaxWorkers(),
                    threadPoolMetrics.getRejectedTotal(), this.backpressureActive);
        }
    }

    public void start() {
        jfrEventFactory.createPeriodicEvent(this);
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


    public void onHighLoad(boolean backpressureActive) {
        final var queueLoad = jfrEventFactory.createEvent(KiwiExecutorQueue.class);
        queueLoad.onEvent(this.name, executionThreadPool.getQueueSize(), executionThreadPool.getQueueCap(),
                executionThreadPool.getActiveWorkers(), executionThreadPool.getMaxWorkers(),
                threadPoolMetrics.getRejectedTotal(), backpressureActive);
        this.backpressureActive = backpressureActive;
    }


    public void onReturnToNormalLoad(boolean backpressureActive) {
        final var queueLoad = jfrEventFactory.createEvent(KiwiExecutorQueue.class);
        queueLoad.onEvent(this.name, executionThreadPool.getQueueSize(), executionThreadPool.getQueueCap(),
                executionThreadPool.getActiveWorkers(), executionThreadPool.getMaxWorkers(),
                threadPoolMetrics.getRejectedTotal(), backpressureActive);
        this.backpressureActive = backpressureActive;
    }

    public String getName() {
        return this.name;
    }

    public int queueSize() {
        return executionThreadPool.getQueueSize();
    }

    public int queueCap() {
        return executionThreadPool.getQueueCap();
    }

    public int activeWorkers() {
        return executionThreadPool.getActiveWorkers();
    }

    public int maxWorkers() {
        return executionThreadPool.getMaxWorkers();
    }

    public int rejectedTotal() {
        return threadPoolMetrics.getRejectedTotal();
    }

    public boolean isBackpressureActive() {
        return this.backpressureActive;
    }
}
