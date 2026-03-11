package com.kiwi.server.validator;

import com.kiwi.server.request.model.TCPRequest;

public interface RequestValidator {
    ValidationResult validate(TCPRequest request);
}
