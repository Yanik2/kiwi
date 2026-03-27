package com.kiwi.server.validator;

import com.kiwi.exception.protocol.ProtocolException;
import com.kiwi.server.request.model.NumericRequest;
import com.kiwi.server.request.model.ParsedRequest;
import com.kiwi.server.request.model.TCPRequest;

import java.util.List;

import static com.kiwi.exception.protocol.ProtocolErrorCode.NON_DIGIT_IN_NUMERIC_VALUE;
import static com.kiwi.exception.protocol.ProtocolErrorCode.VALUE_TOO_LONG;
import static com.kiwi.exception.protocol.ProtocolErrorCode.VALUE_TOO_SHORT;
import static com.kiwi.server.request.Method.EXPIRE;

public class NumericValidator implements RequestValidator {
    private static final int EXPIRE_MAX_VALUE_LENGTH = 16;
    private static final int NUMERIC_LONG_MAX_VALUE_LENGTH = 19;
    private static final short ZERO_ASCII = 48;

    @Override
    public ValidationResult validate(TCPRequest request) {
        final var parsedRequest = (ParsedRequest) request;
        long result = 1;
        if (parsedRequest.getMethod().withValue()) {
            final byte[] byteValue = parsedRequest.getValue();
            final var maxLength = EXPIRE.equals(parsedRequest.getMethod())
                    ? EXPIRE_MAX_VALUE_LENGTH
                    : NUMERIC_LONG_MAX_VALUE_LENGTH;

            if (byteValue.length > maxLength) {
                return new ValidationResult(parsedRequest, List.of(
                        new ProtocolException("Length for value in numeric request is too long", VALUE_TOO_LONG)));
            }
            if (byteValue.length < 1) {
                return new ValidationResult(parsedRequest, List.of(
                        new ProtocolException("Length for value in numeric request is too short", VALUE_TOO_SHORT)));
            }

            final boolean isNegative = byteValue[0] == 45;
            int index;

            if (isNegative) {
                if (byteValue.length < 2) {
                    return new ValidationResult(parsedRequest, List.of(
                            new ProtocolException("Non digit in value for numeric request", NON_DIGIT_IN_NUMERIC_VALUE)));
                }
                index = 1;
            } else {
                index = 0;
            }

            result = 0;

            for (; index < byteValue.length; index++) {
                result *= 10;
                final int digit = byteValue[index] - ZERO_ASCII;
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

            result = isNegative ? -result : result;
        }

        return new ValidationResult(
                new NumericRequest(parsedRequest.getRequestId(),
                        parsedRequest.getFlags(),
                        parsedRequest.getKey(),
                        result,
                        parsedRequest.getMethod()),
                List.of()
        );
    }
}
