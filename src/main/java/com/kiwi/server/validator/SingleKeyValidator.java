package com.kiwi.server.validator;

import com.kiwi.exception.protocol.ProtocolException;
import com.kiwi.server.request.model.ParsedRequest;
import com.kiwi.server.request.model.TCPRequest;

import java.util.List;

import static com.kiwi.exception.protocol.ProtocolErrorCode.SINGLE_KEY_ERROR;

public class SingleKeyValidator implements RequestValidator {
    @Override
    public ValidationResult validate(TCPRequest request) {
        final var parsedRequest = (ParsedRequest) request;
        return parsedRequest.size() == 1 ? new ValidationResult(request, List.of()) : new ValidationResult(request,
                List.of(new ProtocolException("Invalid amount of keys for single key request", SINGLE_KEY_ERROR)));
    }
}
