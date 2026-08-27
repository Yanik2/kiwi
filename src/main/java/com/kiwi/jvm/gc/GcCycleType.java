package com.kiwi.jvm.gc;

import java.util.stream.Stream;

public enum GcCycleType {
    YOUNG_NORMAL("Pause Young (Normal)"),
    YOUNG_MIXED("Pause Young (Mixed)"),
    YOUNG_CONCURRENT("Pause Young (Concurrent Start)"),
    YOUNG_PREPARE_MIXED("Pause Young (Prepare Mixed)"),
    FULL("Pause Full"),
    REMARK("Pause Remark"),
    UNKNOWN("Unknown");

    GcCycleType(String value) {
        this.value = value;
    }

    public static GcCycleType getByMessage(String message) {
        return Stream.of(GcCycleType.values())
                .filter(cycleType -> message.startsWith(cycleType.value))
                .findFirst()
                .orElse(UNKNOWN);
    }

    private final String value;
}
