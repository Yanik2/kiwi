package com.kiwi.observability.factory;

import com.kiwi.observability.metrics.*;
import com.kiwi.observability.MetricsProvider;

import java.util.Map;

public record ObservabilityContainer(
        MetricsProvider metricsProvider,
        RequestMetrics requestMetrics,
        MethodMetrics methodMetrics,
        StorageMetrics storageMetrics,
        Map<String, ThreadPoolMetrics> threadPoolMetrics,
        OperationErrorMetrics operationErrorMetrics,
        ExpirySampleMetrics expirySampleMetrics
) {
}
