package com.kiwi.observability;

import com.kiwi.config.ConfigModule;
import com.kiwi.config.properties.Properties;
import com.kiwi.jvm.provider.JvmInfoProvider;
import com.kiwi.observability.dto.MetricsDataDto;

public class MetricsProvider {

    private final MetricsRegistry metricsRegistry;
    private final JvmInfoProvider jvmInfoProvider;

    public MetricsProvider(MetricsRegistry metricsRegistry, JvmInfoProvider jvmInfoProvider) {
        this.metricsRegistry = metricsRegistry;
        this.jvmInfoProvider = jvmInfoProvider;
    }

    public MetricsDataDto getMetricsInfo() {
        final var gauges = metricsRegistry.getGauges();
        final var counters = metricsRegistry.getCounters();
        final var configHolder = ConfigModule.getConfigurationHolder();
        final var jvmInfo = jvmInfoProvider.getInfo();

        return new MetricsDataDto(
                Properties.PROTOCOL_VERSION,
                Properties.INFO_SCHEMA_VERSION,
                gauges,
                counters,
                metricsRegistry.getServerStart(),
                System.currentTimeMillis() - metricsRegistry.getServerStart(),
                configHolder.getConfig(),
                jvmInfo
        );
    }
}
