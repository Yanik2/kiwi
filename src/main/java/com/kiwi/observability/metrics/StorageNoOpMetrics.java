package com.kiwi.observability.metrics;

public class StorageNoOpMetrics implements StorageMetrics {
    @Override
    public void onTtlExpiredEviction() {

    }
}
