package com.kiwi.observability.factory;

import com.kiwi.observability.metrics.MethodMetrics;
import com.kiwi.observability.MetricsProvider;
import com.kiwi.observability.metrics.OperationErrorMetrics;
import com.kiwi.observability.metrics.RequestMetrics;
import com.kiwi.observability.metrics.StorageMetrics;
import com.kiwi.observability.metrics.ThreadPoolMetrics;

import java.util.Map;

public record ObservabilityContainer(
        MetricsProvider metricsProvider,
        RequestMetrics requestMetrics,
        MethodMetrics methodMetrics,
        StorageMetrics storageMetrics,
        Map<String, ThreadPoolMetrics> threadPoolMetrics,
        OperationErrorMetrics operationErrorMetrics
) {
}
