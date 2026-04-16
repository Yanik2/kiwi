package com.kiwi.observability.factory;

import com.kiwi.observability.metrics.MethodMetrics;
import com.kiwi.observability.MetricsProvider;
import com.kiwi.observability.MetricsRegistry;
import com.kiwi.observability.metrics.OperationErrorMetrics;
import com.kiwi.observability.metrics.RequestMetrics;
import com.kiwi.observability.metrics.StorageMetrics;
import com.kiwi.observability.metrics.ThreadPoolMetrics;

import java.util.Map;

import static com.kiwi.config.properties.Properties.REJECTION_THREAD_POOL_NAME;
import static com.kiwi.config.properties.Properties.SERVER_THREAD_POOL_NAME;

public class ObservabilityModule {

    public static ObservabilityContainer create() {
        return new ObservabilityContainer(
                new MetricsProvider(MetricsRegistry.getInstance()),
                new RequestMetrics(MetricsRegistry.getInstance()),
                new MethodMetrics(MetricsRegistry.getInstance()),
                new StorageMetrics(MetricsRegistry.getInstance()),
                Map.of(SERVER_THREAD_POOL_NAME,
                        new ThreadPoolMetrics(MetricsRegistry.getInstance(), SERVER_THREAD_POOL_NAME),
                        REJECTION_THREAD_POOL_NAME,
                        new ThreadPoolMetrics(MetricsRegistry.getInstance(), REJECTION_THREAD_POOL_NAME)
                ),
                new OperationErrorMetrics(MetricsRegistry.getInstance())
        );
    }
}
