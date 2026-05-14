package com.kiwi.server.request.model;

import com.kiwi.server.request.Method;

public class ConfigRequest extends TCPRequest {
    private final String configKey;

    public ConfigRequest(int requestId, int flags, Method method, String configKey) {
        super(requestId, flags, method);
        this.configKey = configKey;
    }

    public String getConfigKey() {
        return configKey;
    }
}
