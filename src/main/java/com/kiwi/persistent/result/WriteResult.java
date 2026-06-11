package com.kiwi.persistent.result;

import com.kiwi.persistent.model.Value;

public record WriteResult(
        boolean success,
        Value value
) {
}
