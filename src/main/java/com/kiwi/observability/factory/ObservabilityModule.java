package com.kiwi.observability.factory;

import com.kiwi.config.domain.Config;
import com.kiwi.jvm.factory.JvmModuleContainer;
import com.kiwi.observability.metrics.*;
import com.kiwi.observability.MetricsProvider;
import com.kiwi.observability.MetricsRegistry;

import java.util.Map;

import static com.kiwi.config.properties.Properties.REJECTION_THREAD_POOL_NAME;
import static com.kiwi.config.properties.Properties.SERVER_THREAD_POOL_NAME;

public class ObservabilityModule {

    public static ObservabilityContainer create(Config config, JvmModuleContainer jvmModuleContainer) {
        if (config.metricsEnabled()) {
            return new ObservabilityContainer(
                    new MetricsProvider(MetricsRegistry.getInstance(), jvmModuleContainer.jvmInfoProvider()),
                    new RequestMetricsImpl(MetricsRegistry.getInstance()),
                    new MethodMetricsImpl(MetricsRegistry.getInstance()),
                    new StorageMetricsImpl(MetricsRegistry.getInstance()),
                    Map.of(SERVER_THREAD_POOL_NAME,
                            new ThreadPoolMetricsImpl(MetricsRegistry.getInstance(), SERVER_THREAD_POOL_NAME),
                            REJECTION_THREAD_POOL_NAME,
                            new ThreadPoolMetricsImpl(MetricsRegistry.getInstance(), REJECTION_THREAD_POOL_NAME)
                    ),
                    new OperationErrorMetricsImpl(MetricsRegistry.getInstance()),
                    new ExpirySampleMetricsImpl(MetricsRegistry.getInstance())
            );
        } else {
            return new ObservabilityContainer(
                new MetricsProvider(MetricsRegistry.getInstance(), jvmModuleContainer.jvmInfoProvider()),
                    new RequestNoOpMetrics(MetricsRegistry.getInstance()),
                    new MethodNoOpMetrics(),
                    new StorageNoOpMetrics(MetricsRegistry.getInstance()),
                    Map.of(SERVER_THREAD_POOL_NAME,
                            new ThreadPoolNoOpMetrics(),
                            REJECTION_THREAD_POOL_NAME,
                            new ThreadPoolNoOpMetrics()
                    ),
                    new OperationErrorNoOpMetrics(),
                    new ExpirySampleNoOpMetrics()
            );
        }
    }
}
