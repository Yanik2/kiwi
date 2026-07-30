package com.kiwi.server.validator;

import com.kiwi.exception.protocol.ProtocolException;
import com.kiwi.server.request.model.NumericRequest;
import com.kiwi.server.request.model.DefaultRequest;
import com.kiwi.server.request.model.TCPRequest;

import java.util.List;

import static com.kiwi.exception.protocol.ProtocolErrorCode.NON_DIGIT_IN_NUMERIC_VALUE;
import static com.kiwi.exception.protocol.ProtocolErrorCode.NUMERIC_VALUE_IS_TOO_BIG;
import static com.kiwi.exception.protocol.ProtocolErrorCode.VALUE_TOO_LONG;
import static com.kiwi.exception.protocol.ProtocolErrorCode.VALUE_TOO_SHORT;
import static com.kiwi.server.request.Method.EXPIRE;

public class NumericValidator extends SingleKeyValidator {
    private static final int EXPIRE_MAX_VALUE_LENGTH = 16;
    private static final int NUMERIC_LONG_MAX_VALUE_LENGTH = 19;
    private static final short ZERO_ASCII = 48;

    @Override
    public ValidationResult validate(TCPRequest request) {
        final var singleKeyValidation = super.validate(request);
        if (!singleKeyValidation.errors().isEmpty()) {
            return singleKeyValidation;
        }

        final var defaultRequest = (DefaultRequest) request;
        long result = 1;
        if (defaultRequest.getMethod().withValue()) {
            final byte[] byteValue = defaultRequest.getValue();
            final var maxLength = EXPIRE.equals(defaultRequest.getMethod())
                    ? EXPIRE_MAX_VALUE_LENGTH
                    : NUMERIC_LONG_MAX_VALUE_LENGTH;

            if (byteValue.length > maxLength) {
                return new ValidationResult(defaultRequest, List.of(
                        new ProtocolException("Length for value in numeric request is too long", VALUE_TOO_LONG)));
            }
            if (byteValue.length < 1) {
                return new ValidationResult(defaultRequest, List.of(
                        new ProtocolException("Length for value in numeric request is too short", VALUE_TOO_SHORT)));
            }

            final boolean isNegative = byteValue[0] == 45;
            int index;

            if (isNegative) {
                if (byteValue.length < 2) {
                    return new ValidationResult(defaultRequest, List.of(
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
                    return new ValidationResult(defaultRequest, List.of(
                            new ProtocolException("Non digit in value for expiration request", NON_DIGIT_IN_NUMERIC_VALUE)));
                }

                if ((result + digit) < result) {
                    return new ValidationResult(defaultRequest, List.of(
                            new ProtocolException("Overflow in request numeric value", NUMERIC_VALUE_IS_TOO_BIG)
                    ));
                }
                result += digit;
            }

            result = EXPIRE.equals(defaultRequest.getMethod()) ? result * 1000 : result;
            if (result < 0) {
                return new ValidationResult(defaultRequest, List.of(
                        new ProtocolException("Seconds value is too big for expiration request", VALUE_TOO_LONG)));
            }

            result = isNegative ? -result : result;
        }

        return new ValidationResult(
                new NumericRequest(defaultRequest.getRequestId(),
                        defaultRequest.getFlags(),
                        defaultRequest.getKey(),
                        result,
                        defaultRequest.getMethod(),
                        defaultRequest.getKiwiRequest()),
                List.of()
        );
    }
}
