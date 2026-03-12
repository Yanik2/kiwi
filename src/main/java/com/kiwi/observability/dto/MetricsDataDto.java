package com.kiwi.observability.dto;

import java.util.Map;

public record MetricsDataDto(
    String protocolVersion,
    String infoSchemaVersion,
    Map<String, Integer> gauges,
    Map<String, Long> counters,
    long serverStart,
    long serverUptime
) {}
