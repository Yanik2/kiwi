package com.kiwi.server.validator;


import static com.kiwi.exception.protocol.ProtocolErrorCode.NON_DIGIT_IN_NUMERIC_VALUE;
import static com.kiwi.exception.protocol.ProtocolErrorCode.VALUE_TOO_LONG;
import static com.kiwi.exception.protocol.ProtocolErrorCode.VALUE_TOO_SHORT;
import static com.kiwi.server.Method.EXPIRE;

import com.kiwi.exception.protocol.ProtocolException;
import com.kiwi.server.dto.ExpireRequest;
import com.kiwi.server.dto.ParsedRequest;
import com.kiwi.server.dto.TCPRequest;

import java.util.List;

public class ExpireValidator implements RequestValidator {
    private static final int EXPIRE_MAX_VALUE_LENGTH = 16;
    private static final int PEXPIRE_MAX_VALUE_LENGTH = 19;
    private static final short ZERO_ASCII = 48;

    @Override
    public ValidationResult validate(TCPRequest request) {
        final var parsedRequest = (ParsedRequest) request;
        final byte[] value = parsedRequest.getValue();
        final var maxLength = EXPIRE.equals(parsedRequest.getMethod())
                ? EXPIRE_MAX_VALUE_LENGTH
                : PEXPIRE_MAX_VALUE_LENGTH;

        if (value.length > maxLength) {
            return new ValidationResult(parsedRequest, List.of(
                    new ProtocolException("Length for value in expiration request is too long", VALUE_TOO_LONG)));
        }
        if (value.length < 1) {
            return new ValidationResult(parsedRequest, List.of(
                    new ProtocolException("Length for value in expiration request is too short", VALUE_TOO_SHORT)));
        }

        final boolean isNegative = value[0] == 45;
        int index;

        if (isNegative) {
            if (value.length < 2) {
                return new ValidationResult(parsedRequest, List.of(
                        new ProtocolException("Non digit in value for expiration request", NON_DIGIT_IN_NUMERIC_VALUE)));
            }
            index = 1;
        } else {
            index = 0;
        }

        long result = 0;

        for (; index < value.length; index++) {
            result *= 10;
            final int digit = value[index] - ZERO_ASCII;
            if (digit < 0 || digit > 9) {
                return new ValidationResult(parsedRequest, List.of(
                        new ProtocolException("Non digit in value for expiration request", NON_DIGIT_IN_NUMERIC_VALUE)));
            }

            result += digit;
        }

        result = EXPIRE.equals(parsedRequest.getMethod()) ? result * 1000 : result;
        if (result < 0) {
            return new ValidationResult(parsedRequest, List.of(
                    new ProtocolException("Seconds value is too big for expiration request", VALUE_TOO_LONG)));
        }

        return new ValidationResult(
                new ExpireRequest(
                        parsedRequest.getRequestId(),
                        parsedRequest.getFlags(),
                        parsedRequest.getMethod(),
                        parsedRequest.getKey(),
                        isNegative ? -result : result),
                List.of()
        );
    }
}
