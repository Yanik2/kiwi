package com.kiwi.observability.metrics;

public interface StorageMetrics {
    void onTtlExpiredEviction();
}
