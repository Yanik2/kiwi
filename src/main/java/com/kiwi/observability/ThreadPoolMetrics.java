package com.kiwi.observability;

public class ThreadPoolMetrics {
    private final MetricsRegistry metricsRegistry;
    private final String threadPoolName;

    public ThreadPoolMetrics(MetricsRegistry metricsRegistry, String threadPoolName) {
        this.metricsRegistry = metricsRegistry;
        this.threadPoolName = threadPoolName;
    }

    public String getThreadPoolName() {
        return threadPoolName;
    }

    public void registerMetrics() {
        metricsRegistry.registerThreadPool(threadPoolName);
    }

    public void onWorkersMax(int workersMax) {
        metricsRegistry.addWorkersMax(threadPoolName, workersMax);
    }

    public void setWorkersActive(int workersActive) {
        metricsRegistry.addWorkersActive(threadPoolName, workersActive);
    }

    public void onQueueSize(int queueSize) {
        metricsRegistry.addQueueSize(threadPoolName, queueSize);
    }

    public void onTaskEnqueued() {
        metricsRegistry.addTaskEnqueued(threadPoolName);
    }

    public void onTaskCompleted() {
        metricsRegistry.addTaskCompleted(threadPoolName);
    }

    public void onTaskRejected() {
        metricsRegistry.addTaskRejected(threadPoolName);
    }

}
