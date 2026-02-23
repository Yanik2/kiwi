package com.kiwi.server.validator;

import com.kiwi.server.dto.TCPRequest;

public interface RequestValidator {
    ValidationResult validate(TCPRequest request);
}
