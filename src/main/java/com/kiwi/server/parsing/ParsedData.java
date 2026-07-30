package com.kiwi.server.parsing;

import com.kiwi.server.request.Method;
import com.kiwi.server.request.model.DefaultRequest;

import java.util.List;

public record ParsedData(
        int flags,
        Method method,
        List<DefaultRequest.KeyValuePair> keyValuePairs
) {
}
