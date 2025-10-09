package com.kiwi.server.validator;

import com.kiwi.server.dto.ParsedRequest;
import com.kiwi.server.dto.TCPRequest;

public interface RequestValidator {
    TCPRequest validate(ParsedRequest request);
}
