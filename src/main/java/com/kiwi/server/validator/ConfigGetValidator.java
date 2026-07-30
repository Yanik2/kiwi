package com.kiwi.server.validator;

import com.kiwi.exception.protocol.ProtocolException;
import com.kiwi.server.request.model.ConfigRequest;
import com.kiwi.server.request.model.DefaultRequest;
import com.kiwi.server.request.model.TCPRequest;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static com.kiwi.exception.protocol.ProtocolErrorCode.SINGLE_KEY_ERROR;

public class ConfigGetValidator implements RequestValidator {
    @Override
    public ValidationResult validate(TCPRequest request) {
        final var defaultRequest = (DefaultRequest) request;
        if (defaultRequest.size() != 1) {
            return new ValidationResult(request, List.of(new ProtocolException("Config request requires one key",
                    SINGLE_KEY_ERROR)));
        }

        final var key = defaultRequest.getKey();

        if (key.length == 0) {
            return new ValidationResult(request, List.of(new ProtocolException("Key is empty", SINGLE_KEY_ERROR)));
        }
        final var keyString = new String(key, StandardCharsets.UTF_8).trim();

        final var spaceIndex = keyString.indexOf(32);

        if (spaceIndex == -1) {
            return new ValidationResult(new ConfigRequest(defaultRequest.getRequestId(), defaultRequest.getFlags(),
                    defaultRequest.getMethod(), keyString, defaultRequest.getKiwiRequest()), List.of());
        } else {
            return new ValidationResult(request, List.of(new ProtocolException("Invalid config key", SINGLE_KEY_ERROR)));
        }
    }
}
