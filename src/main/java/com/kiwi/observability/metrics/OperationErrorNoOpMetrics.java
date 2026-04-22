package com.kiwi.observability.metrics;

import com.kiwi.persistent.mutation.ErrorType;

public class OperationErrorNoOpMetrics implements OperationErrorMetrics {
    @Override
    public void onError(ErrorType error) {
    }
}
