package com.kiwi.observability.dto;

import com.kiwi.config.domain.Config;
import com.kiwi.jvm.JvmInfoSnapshot;

import java.util.Map;

public record MetricsDataDto(
    String protocolVersion,
    String infoSchemaVersion,
    Map<String, Long> gauges,
    Map<String, Long> counters,
    long serverStart,
    long serverUptime,
    Config config,
    JvmInfoSnapshot jvmInfoSnapshot
) {}
