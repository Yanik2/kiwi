package com.kiwi.server.validator;

import com.kiwi.server.dto.ParsedRequest;
import com.kiwi.server.dto.TCPRequest;

public class NoOpValidator implements RequestValidator {
    private static final NoOpValidator instance = new NoOpValidator();

    public static NoOpValidator getInstance() {
        return instance;
    }

    @Override
    public TCPRequest validate(ParsedRequest request) {
        return request;
    }
}
