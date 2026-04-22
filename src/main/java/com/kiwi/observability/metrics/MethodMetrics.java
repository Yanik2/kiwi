package com.kiwi.observability.metrics;

import com.kiwi.server.request.Method;

public interface MethodMetrics {
    void onRequest(Method method);
}
