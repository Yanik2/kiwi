package com.kiwi.observability.metrics;

import com.kiwi.server.request.Method;

public class MethodNoOpMetrics implements MethodMetrics {
    @Override
    public void onRequest(Method method) {
    }
}
