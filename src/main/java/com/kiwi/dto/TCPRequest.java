package com.kiwi.dto;

import com.kiwi.persistent.dto.Key;
import com.kiwi.persistent.dto.Value;
import com.kiwi.server.Method;

public record TCPRequest(
    Method method,
    Key key,
    Value value
) {
    public TCPRequest(Method method, Key key) {
        this(method, key, null);
    }

    public TCPRequest(Method method) {
        this(method, null);
    }
}
