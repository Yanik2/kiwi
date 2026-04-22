package com.kiwi.observability.metrics;

public interface ThreadPoolMetrics {
    void onWorkersMax(int workersMax);
    void setWorkersActive(int workersActive);
    void onQueueSize(int queueSize);
    void onTaskEnqueued();
    void onTaskCompleted();
    void onTaskRejected();
    void onBpPaused(int delta);
    void onBpPauses();
}
