package com.kiwi.observability.factory;

import com.kiwi.config.domain.Config;
import com.kiwi.observability.metrics.MethodMetricsImpl;
import com.kiwi.observability.MetricsProvider;
import com.kiwi.observability.MetricsRegistry;
import com.kiwi.observability.metrics.MethodNoOpMetrics;
import com.kiwi.observability.metrics.OperationErrorMetricsImpl;
import com.kiwi.observability.metrics.OperationErrorNoOpMetrics;
import com.kiwi.observability.metrics.RequestMetricsImpl;
import com.kiwi.observability.metrics.RequestNoOpMetrics;
import com.kiwi.observability.metrics.StorageMetricsImpl;
import com.kiwi.observability.metrics.StorageNoOpMetrics;
import com.kiwi.observability.metrics.ThreadPoolMetricsImpl;
import com.kiwi.observability.metrics.ThreadPoolNoOpMetrics;

import java.util.Map;

import static com.kiwi.config.properties.Properties.REJECTION_THREAD_POOL_NAME;
import static com.kiwi.config.properties.Properties.SERVER_THREAD_POOL_NAME;

public class ObservabilityModule {

    public static ObservabilityContainer create(Config config) {
        if (config.metricsEnabled()) {
            return new ObservabilityContainer(
                    new MetricsProvider(MetricsRegistry.getInstance()),
                    new RequestMetricsImpl(MetricsRegistry.getInstance()),
                    new MethodMetricsImpl(MetricsRegistry.getInstance()),
                    new StorageMetricsImpl(MetricsRegistry.getInstance()),
                    Map.of(SERVER_THREAD_POOL_NAME,
                            new ThreadPoolMetricsImpl(MetricsRegistry.getInstance(), SERVER_THREAD_POOL_NAME),
                            REJECTION_THREAD_POOL_NAME,
                            new ThreadPoolMetricsImpl(MetricsRegistry.getInstance(), REJECTION_THREAD_POOL_NAME)
                    ),
                    new OperationErrorMetricsImpl(MetricsRegistry.getInstance())
            );
        } else {
            return new ObservabilityContainer(
                new MetricsProvider(MetricsRegistry.getInstance()),
                    new RequestNoOpMetrics(MetricsRegistry.getInstance()),
                    new MethodNoOpMetrics(),
                    new StorageNoOpMetrics(),
                    Map.of(SERVER_THREAD_POOL_NAME,
                            new ThreadPoolNoOpMetrics(),
                            REJECTION_THREAD_POOL_NAME,
                            new ThreadPoolNoOpMetrics()
                    ),
                    new OperationErrorNoOpMetrics()
            );
        }
    }
}
