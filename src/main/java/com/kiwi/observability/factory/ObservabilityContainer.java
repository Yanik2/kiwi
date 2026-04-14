package com.kiwi.observability.factory;

import com.kiwi.observability.MethodMetrics;
import com.kiwi.observability.MetricsProvider;
import com.kiwi.observability.OperationErrorMetrics;
import com.kiwi.observability.RequestMetrics;
import com.kiwi.observability.StorageMetrics;
import com.kiwi.observability.ThreadPoolMetrics;

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
