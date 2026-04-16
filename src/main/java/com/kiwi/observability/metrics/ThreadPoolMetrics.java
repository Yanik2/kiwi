package com.kiwi.observability.metrics;

import com.kiwi.observability.MetricsRegistry;

import static com.kiwi.observability.util.MetricKeys.BP_PAUSED_COUNT;
import static com.kiwi.observability.util.MetricKeys.BP_PAUSE_COUNT;
import static com.kiwi.observability.util.MetricKeys.TP_QUEUE_SIZE;
import static com.kiwi.observability.util.MetricKeys.TP_TASK_COMPLETED;
import static com.kiwi.observability.util.MetricKeys.TP_TASK_ENQUEUED;
import static com.kiwi.observability.util.MetricKeys.TP_TASK_REJECTED;
import static com.kiwi.observability.util.MetricKeys.TP_WORKERS_ACTIVE;
import static com.kiwi.observability.util.MetricKeys.TP_WORKERS_MAX;

public class ThreadPoolMetrics {
    private final MetricsRegistry metricsRegistry;
    private final String threadPoolName;

    public ThreadPoolMetrics(MetricsRegistry metricsRegistry, String threadPoolName) {
        this.metricsRegistry = metricsRegistry;
        this.threadPoolName = threadPoolName;

        metricsRegistry.registerCounter(threadPoolName + TP_WORKERS_MAX);
        metricsRegistry.registerGauge(threadPoolName + TP_WORKERS_ACTIVE);
        metricsRegistry.registerGauge(threadPoolName + TP_QUEUE_SIZE);
        metricsRegistry.registerCounter(threadPoolName + TP_TASK_ENQUEUED);
        metricsRegistry.registerCounter(threadPoolName + TP_TASK_COMPLETED);
        metricsRegistry.registerCounter(threadPoolName + TP_TASK_REJECTED);
        metricsRegistry.registerCounter(threadPoolName + BP_PAUSE_COUNT);
        metricsRegistry.registerGauge(threadPoolName + BP_PAUSED_COUNT);
    }

    public void onWorkersMax(int workersMax) {
        metricsRegistry.updateCounter(threadPoolName + TP_WORKERS_MAX, workersMax);
    }

    public void setWorkersActive(int workersActive) {
        metricsRegistry.updateGauge(threadPoolName + TP_WORKERS_ACTIVE, workersActive);
    }

    public void onQueueSize(int queueSize) {
        metricsRegistry.updateGauge(threadPoolName + TP_QUEUE_SIZE, queueSize);
    }

    public void onTaskEnqueued() {
        metricsRegistry.updateCounter(threadPoolName + TP_TASK_ENQUEUED);
    }

    public void onTaskCompleted() {
        metricsRegistry.updateCounter(threadPoolName + TP_TASK_COMPLETED);
    }

    public void onTaskRejected() {
        metricsRegistry.updateCounter(threadPoolName + TP_TASK_REJECTED);
    }

    public void onBpPaused(int delta) {
        metricsRegistry.updateGauge(threadPoolName + BP_PAUSED_COUNT, delta);
    }

    public void onBpPauses() {
        metricsRegistry.updateCounter(threadPoolName + BP_PAUSE_COUNT);
    }

}
