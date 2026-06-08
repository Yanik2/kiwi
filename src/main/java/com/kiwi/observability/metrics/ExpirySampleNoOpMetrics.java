package com.kiwi.observability.metrics;

public class ExpirySampleNoOpMetrics implements ExpirySampleMetrics {
    @Override
    public void onTtlScanned(int delta) {

    }

    @Override
    public void onActiveExpiredEvictions(int delta) {

    }
}
