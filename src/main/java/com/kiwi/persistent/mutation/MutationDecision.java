package com.kiwi.persistent.mutation;

import com.kiwi.persistent.model.Value;

public sealed interface MutationDecision {

    record Write(boolean success, Value value, Value returnValue) implements MutationDecision {
        public Write(boolean success, Value value) {
            this(success, value, null);
        }
    }

    record Delete(boolean success) implements MutationDecision {
    }

    record NoOp(boolean success) implements MutationDecision {
    }

    record Error() implements MutationDecision {
    }
}
