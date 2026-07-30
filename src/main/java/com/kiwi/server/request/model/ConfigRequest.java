package com.kiwi.server.request.model;

import com.kiwi.jvm.jfr.event.KiwiRequest;
import com.kiwi.server.request.Method;

public class ConfigRequest extends TCPRequest {
    private final String configKey;

    public ConfigRequest(int requestId, int flags, Method method, String configKey, KiwiRequest kiwiRequest) {
        super(requestId, flags, method, kiwiRequest);
        this.configKey = configKey;
    }

    public String getConfigKey() {
        return configKey;
    }
}
