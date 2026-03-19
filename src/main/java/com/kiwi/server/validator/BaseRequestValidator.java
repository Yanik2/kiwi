package com.kiwi.server.validator;

import static com.kiwi.server.request.Method.EXPIRE;
import static com.kiwi.server.request.Method.PEXPIRE;

import com.kiwi.server.request.Method;
import com.kiwi.server.request.model.TCPRequest;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

public class BaseRequestValidator implements RequestValidator {
    private final Map<Method, RequestValidator> requestValidators;

    public BaseRequestValidator() {
        final var expireValidator = new ExpireValidator();
        final var validators = new EnumMap<Method, RequestValidator>(Method.class);

        validators.put(EXPIRE, expireValidator);
        validators.put(PEXPIRE, expireValidator);

        this.requestValidators = Collections.unmodifiableMap(validators);
    }

    @Override
    public ValidationResult validate(TCPRequest request) {
        return requestValidators.getOrDefault(request.getMethod(), NoOpValidator.getInstance()).validate(request);
    }
}
