package com.kiwi.config.domain;

public record JvmConfig(
        boolean jvmInfoEnabled,
        boolean jfrEnabled,
        String jfrDir,
        int jfrMaxAgeSeconds,
        int jfrMaxSizeBytes,
        JvmBuffersStrategy buffersStrategy,
        boolean buffersPoolingEnabled,
        boolean buffersLeakTrackingEnabled,
        boolean arenaEnabled,
        boolean arenaDebugPoisoning,
        boolean safepointWatchdogEnabled,
        int safepointWatchdogPeriodMs
) {
}
