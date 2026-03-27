package com.kiwi.exception.protocol;

public enum ProtocolErrorCode {
    UNKNOWN_METHOD,
    VALUE_TOO_LONG,
    VALUE_TOO_SHORT,
    NUMERIC_VALUE_IS_TOO_BIG,
    UNEXPECTED_EOF,
    NON_DIGIT_IN_NUMERIC_VALUE,
    INVALID_SEPARATOR,
    INVALID_HEADER,
    BUFFER_ERROR
}
