package com.kiwi.persistent.mutation;

import com.kiwi.persistent.model.Key;
import com.kiwi.persistent.model.Value;

public record MutationResult(
        Key key,
        Value value,
        boolean success
) {
    MutationResult(Key key, boolean success) {
        this(key, null, success);
    }
}
