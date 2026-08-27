package com.kiwi.jvm.gc;

public record GcLogParsedMessage(
        GcCycleType cycleType,
        long heapSizeBefore,
        long heapSizeAfter,
        long heapTotalSize,
        float timeSpent,
        String originalMessage
) {
}
