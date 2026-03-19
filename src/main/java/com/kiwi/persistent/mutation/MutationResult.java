package com.kiwi.persistent.mutation;

import com.kiwi.persistent.model.Key;
import com.kiwi.persistent.model.Value;

import java.util.Optional;

public record MutationResult(
        Key key,
        Optional<Value> value,
        boolean success
) {
}
