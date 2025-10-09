package com.kiwi.exception.protocol;

public enum ProtocolErrorCode {
    UNKNOWN_METHOD,
    HEADER_LEN_TOO_LONG,
    VALUE_TOO_LONG,
    KEY_TOO_LONG,
    UNEXPECTED_EOF,
    NON_DIGIT_IN_NUMERIC_VALUE,
    INVALID_SEPARATOR,
    METHOD_TOO_LONG
}
