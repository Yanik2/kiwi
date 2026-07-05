package com.kiwi.config.domain;

public record Config(
        int port,
        int backlog,
        int maxClients,
        int soTimeoutMillis,
        boolean metricsEnabled,
        int ttlSamplerPeriodMs,
        int ttlSampleBatch,
        int ttlBackoffMaxMs,
        int memoryMaxBytes,
        EvictionPolicy evictionPolicy,
        JvmConfig jvmConfig
) {
}
