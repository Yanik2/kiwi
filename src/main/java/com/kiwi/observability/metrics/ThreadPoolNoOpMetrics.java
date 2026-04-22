package com.kiwi.observability.metrics;

public class ThreadPoolNoOpMetrics implements ThreadPoolMetrics {
    @Override
    public void onWorkersMax(int workersMax) {

    }

    @Override
    public void setWorkersActive(int workersActive) {

    }

    @Override
    public void onQueueSize(int queueSize) {

    }

    @Override
    public void onTaskEnqueued() {

    }

    @Override
    public void onTaskCompleted() {

    }

    @Override
    public void onTaskRejected() {

    }

    @Override
    public void onBpPaused(int delta) {

    }

    @Override
    public void onBpPauses() {

    }
}
