package com.kiwi.persistent.mutation;

import com.kiwi.persistent.model.Value;

public record CurrentState(
        boolean exists,
        Value value
) {
}
