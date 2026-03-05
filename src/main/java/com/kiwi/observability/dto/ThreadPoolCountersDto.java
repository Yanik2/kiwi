package com.kiwi.observability.dto;

public record ThreadPoolCountersDto(
    int workersMax,
    int workersActive,
    int queueSize,
    long taskEnqueued,
    long taskCompleted,
    long taskRejected,
    int bpPaused,
    long bpPauses,
    long bpRejectClose
) {
}
