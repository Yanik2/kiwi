package com.kiwi.jvm;

import java.util.Map;

public record JvmInfoSnapshot(
        Map<String, Object> metrics
) {
}
