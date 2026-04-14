package com.kiwi.persistent.storage;

import com.kiwi.persistent.model.Key;
import com.kiwi.persistent.model.Value;
import com.kiwi.persistent.mutation.Mutation;
import com.kiwi.persistent.mutation.MutationResult;

import java.util.Optional;

public interface Storage {
    Optional<Value> read(Key key);

    void write(Key key, Value value);

    MutationResult mutate(Key key, Mutation mutation);

    void delete(Key key);

    int size();
}
