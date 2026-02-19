package com.kiwi.server;

import static com.kiwi.server.Method.DEL;
import static com.kiwi.server.Method.EXPIRE;
import static com.kiwi.server.Method.EXT;
import static com.kiwi.server.Method.GET;
import static com.kiwi.server.Method.INF;
import static com.kiwi.server.Method.PERSIST;
import static com.kiwi.server.Method.PEXPIRE;
import static com.kiwi.server.Method.PING;
import static com.kiwi.server.Method.PTTL;
import static com.kiwi.server.Method.SET;
import static com.kiwi.server.Method.TTL;

import com.kiwi.server.dto.ParsedRequest;
import com.kiwi.server.dto.TCPRequest;
import com.kiwi.server.validator.ExpireValidator;
import com.kiwi.server.validator.NoOpValidator;
import com.kiwi.server.validator.RequestValidator;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

// THIS CLASS WILL BE REFACTORED ON IMPLEMENTING MULTITHREADED PROCESSING
public class BaseRequestValidator implements RequestValidator {
    private final Map<Method, com.kiwi.server.validator.RequestValidator> requestValidators;

    public BaseRequestValidator() {
        final var expireValidator = new ExpireValidator();
        final var validators = new EnumMap<Method, com.kiwi.server.validator.RequestValidator>(Method.class);

        validators.put(GET, NoOpValidator.getInstance());
        validators.put(SET, NoOpValidator.getInstance());
        validators.put(DEL, NoOpValidator.getInstance());
        validators.put(EXT, NoOpValidator.getInstance());
        validators.put(INF, NoOpValidator.getInstance());
        validators.put(PING, NoOpValidator.getInstance());
        validators.put(EXPIRE, expireValidator);
        validators.put(PEXPIRE, expireValidator);
        validators.put(PERSIST, NoOpValidator.getInstance());
        validators.put(TTL, NoOpValidator.getInstance());
        validators.put(PTTL, NoOpValidator.getInstance());

        this.requestValidators = Collections.unmodifiableMap(validators);
    }

    @Override
    public TCPRequest validate(ParsedRequest request) {
        return requestValidators.getOrDefault(request.getMethod(), NoOpValidator.getInstance()).validate(request);
    }
}
