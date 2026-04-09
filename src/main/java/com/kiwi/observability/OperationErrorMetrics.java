package com.kiwi.observability;

import com.kiwi.persistent.mutation.ErrorType;

import static com.kiwi.observability.util.MetricKeys.OP_NOT_EXISTS;
import static com.kiwi.observability.util.MetricKeys.OP_RANGE_ERROR;
import static com.kiwi.observability.util.MetricKeys.OP_WRONG_TYPE;

public class OperationErrorMetrics {
    private final MetricsRegistry metricsRegistry;

    public OperationErrorMetrics(MetricsRegistry metricsRegistry) {
        this.metricsRegistry = metricsRegistry;

        metricsRegistry.registerCounter(OP_WRONG_TYPE);
        metricsRegistry.registerCounter(OP_RANGE_ERROR);
        metricsRegistry.registerCounter(OP_NOT_EXISTS);
    }

    public void onError(ErrorType errorType) {
        switch (errorType) {
            case WRONG_TYPE -> metricsRegistry.updateCounter(OP_WRONG_TYPE);
            case RANGE -> metricsRegistry.updateCounter(OP_RANGE_ERROR);
            case NOT_EXISTS -> metricsRegistry.updateCounter(OP_NOT_EXISTS);
        }
    }
}
