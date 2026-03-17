package com.kiwi.persistent.mutation;

public interface Mutation {
    MutationDecision apply(CurrentState state);
}
