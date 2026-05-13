package com.kiwi.log;

import com.kiwi.server.request.Method;

public record RequestContext(
        int requestId,
        Method method
) {
}
