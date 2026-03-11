package com.kiwi.server.validator;

import com.kiwi.server.request.model.TCPRequest;

import java.util.List;

public class NoOpValidator implements RequestValidator {
    private static final NoOpValidator instance = new NoOpValidator();

    public static NoOpValidator getInstance() {
        return instance;
    }

    @Override
    public ValidationResult validate(TCPRequest request) {
        return new ValidationResult(request, List.of());
    }
}
