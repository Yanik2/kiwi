package com.kiwi.observability.metrics;

import com.kiwi.persistent.mutation.ErrorType;

public interface OperationErrorMetrics {
    void onError(ErrorType error);
}
