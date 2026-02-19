package com.kiwi.server.validator;

import static com.kiwi.server.Method.EXPIRE;

import com.kiwi.exception.protocol.ProtocolErrorCode;
import com.kiwi.exception.protocol.ProtocolException;
import com.kiwi.server.dto.ExpireRequest;
import com.kiwi.server.dto.ParsedRequest;
import com.kiwi.server.dto.TCPRequest;

public class ExpireValidator implements RequestValidator {
    private static final int EXPIRE_MAX_VALUE_LENGTH = 16;
    private static final int PEXPIRE_MAX_VALUE_LENGTH = 19;
    private static final short ZERO_ASCII = 48;

    @Override
    public TCPRequest validate(ParsedRequest request) {
        final byte[] value = request.getValue();
        final var maxLength = EXPIRE.equals(request.getMethod())
            ? EXPIRE_MAX_VALUE_LENGTH
            : PEXPIRE_MAX_VALUE_LENGTH;

        if (value.length > maxLength) {
            throw new ProtocolException("Length for value in expiration request is too long",
                ProtocolErrorCode.VALUE_TOO_LONG);
        }
        if (value.length < 1) {
            throw new ProtocolException("Length for value in expiration request is too short",
                ProtocolErrorCode.VALUE_TOO_SHORT);
        }

        final boolean isNegative = value[0] == 45;
        int index;

        if (isNegative) {
            if (value.length < 2) {
                throw new ProtocolException("Non digit in value for expiration request",
                    ProtocolErrorCode.NON_DIGIT_IN_NUMERIC_VALUE);
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
                throw new ProtocolException("Non digit in value for expiration request",
                    ProtocolErrorCode.NON_DIGIT_IN_NUMERIC_VALUE);
            }

            result += digit;
        }

        result = EXPIRE.equals(request.getMethod()) ? result * 1000 : result;
        if (result < 0) {
            throw new ProtocolException("Seconds value is too big for expiration request",
                ProtocolErrorCode.VALUE_TOO_LONG);
        }

        return new ExpireRequest(request.getFlags(), request.getMethod(), request.getKey(),
            isNegative ? -result : result);
    }
}
