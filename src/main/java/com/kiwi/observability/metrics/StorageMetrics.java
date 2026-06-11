package com.kiwi.observability.metrics;

public interface StorageMetrics {
    void onTtlExpiredEviction();
    void onMemoryBytes(int delta);
    long getMemoryUsedBytes();
    void onEvictionTriggered();
}
