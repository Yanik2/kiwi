package com.kiwi.persistent.mutation;

import com.kiwi.persistent.model.Value;

public sealed interface MutationDecision {

    record Write(Value value) implements MutationDecision {
    }

    record Delete() implements MutationDecision {
    }

    record NoOp() implements MutationDecision {
    }

    record Error() implements MutationDecision {
    }
}
