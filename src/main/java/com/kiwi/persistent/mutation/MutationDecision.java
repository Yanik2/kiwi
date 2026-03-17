package com.kiwi.persistent.mutation;

import com.kiwi.persistent.model.Key;
import com.kiwi.persistent.model.Value;

import java.util.Map;

public interface MutationDecision {
    MutationResult applyDecision(Map<Key, Value> storage, Key key);

    record Write(Value value) implements MutationDecision {
        @Override
        public MutationResult applyDecision(Map<Key, Value> storage, Key key) {
            storage.put(key, value);
            return new MutationResult(key, value, true);
        }
    }

    record Delete() implements MutationDecision {
        @Override
        public MutationResult applyDecision(Map<Key, Value> storage, Key key) {
            storage.remove(key);
            return new MutationResult(key, true);
        }
    }

    record NoOp() implements MutationDecision {
        @Override
        public MutationResult applyDecision(Map<Key, Value> storage, Key key) {
            return new MutationResult(key, true);
        }
    }

    record Error() implements MutationDecision {
        @Override
        public MutationResult applyDecision(Map<Key, Value> storage, Key key) {
            return new MutationResult(key, false);
        }
    }
}
