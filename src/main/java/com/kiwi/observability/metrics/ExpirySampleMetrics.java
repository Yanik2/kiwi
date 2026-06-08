package com.kiwi.observability.metrics;

public interface ExpirySampleMetrics {
    void onTtlScanned(int delta);

    void onActiveExpiredEvictions(int delta);
}
