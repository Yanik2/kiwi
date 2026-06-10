package com.kiwi.observability.metrics;

public class StorageNoOpMetrics implements StorageMetrics {
    @Override
    public void onTtlExpiredEviction() {

    }

    @Override
    public void onMemoryBytes(int delta) {

    }

    @Override
    public long getMemoryUsedBytes() {
        // when metrics are disabled it always returns 0
        return 0;
    }
}
