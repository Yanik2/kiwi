package com.kiwi.server.validator;

import static com.kiwi.server.request.Method.DECR;
import static com.kiwi.server.request.Method.DECRBY;
import static com.kiwi.server.request.Method.DEL;
import static com.kiwi.server.request.Method.EXPIRE;
import static com.kiwi.server.request.Method.GET;
import static com.kiwi.server.request.Method.GETSET;
import static com.kiwi.server.request.Method.INCR;
import static com.kiwi.server.request.Method.INCRBY;
import static com.kiwi.server.request.Method.PERSIST;
import static com.kiwi.server.request.Method.PEXPIRE;
import static com.kiwi.server.request.Method.PTTL;
import static com.kiwi.server.request.Method.SET;
import static com.kiwi.server.request.Method.SETNX;
import static com.kiwi.server.request.Method.TTL;

import com.kiwi.server.request.Method;
import com.kiwi.server.request.model.TCPRequest;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

public class BaseRequestValidator implements RequestValidator {
    private final Map<Method, RequestValidator> requestValidators;

    public BaseRequestValidator() {
        final var numericValidator = new NumericValidator();
        final var singleKeyValidator = new SingleKeyValidator();
        final var validators = new EnumMap<Method, RequestValidator>(Method.class);

        validators.put(EXPIRE, numericValidator);
        validators.put(PEXPIRE, numericValidator);
        validators.put(INCRBY, numericValidator);
        validators.put(DECRBY, numericValidator);
        validators.put(INCR, numericValidator);
        validators.put(DECR, numericValidator);
        validators.put(GET, singleKeyValidator);
        validators.put(SET, singleKeyValidator);
        validators.put(DEL, singleKeyValidator);
        validators.put(PERSIST, singleKeyValidator);
        validators.put(TTL, singleKeyValidator);
        validators.put(PTTL, singleKeyValidator);
        validators.put(SETNX, singleKeyValidator);
        validators.put(GETSET, singleKeyValidator);

        this.requestValidators = Collections.unmodifiableMap(validators);
    }

    @Override
    public ValidationResult validate(TCPRequest request) {
        return requestValidators.getOrDefault(request.getMethod(), NoOpValidator.getInstance()).validate(request);
    }
}
