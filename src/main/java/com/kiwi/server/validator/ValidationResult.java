package com.kiwi.server.validator;

import com.kiwi.exception.protocol.ProtocolException;
import com.kiwi.server.dto.TCPRequest;

import java.util.List;

public record ValidationResult(
        TCPRequest request,
        List<ProtocolException> errors
) {
}
