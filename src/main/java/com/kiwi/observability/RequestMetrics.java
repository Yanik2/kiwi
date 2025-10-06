package com.kiwi.observability;

import com.kiwi.exception.protocol.ProtocolErrorCode;

public final class RequestMetrics {
    private final MetricsRegistry metricsRegistry;

    RequestMetrics(MetricsRegistry metricsRegistry) {
        this.metricsRegistry = metricsRegistry;
    }

    public void onAccept() {
        metricsRegistry.addAcceptConnection();
    }

    public void onClose() {
        metricsRegistry.addCloseConnection();
    }

    public void onParse(long bytes) {
        metricsRegistry.addParsedBytes(bytes);
    }

    public void onWrite(long bytes) {
        metricsRegistry.addWrittenBytes(bytes);
    }

    public void onProtoError(ProtocolErrorCode protocolErrorCode) {
        switch (protocolErrorCode) {
            case KEY_TOO_LONG -> metricsRegistry.addKeyTooLongError();
            case UNKNOWN_METHOD -> metricsRegistry.addUnknownMethodError();
            case HEADER_LEN_TOO_LONG -> metricsRegistry.addHeaderTooLongError();
            case VALUE_TOO_LONG -> metricsRegistry.addValueTooLongError();
            case UNEXPECTED_EOF -> metricsRegistry.addUnexpectedEndOfFileError();
            case NON_DIGIT_IN_LENGTH -> metricsRegistry.addNonDigitInLengthError();
            case INVALID_SEPARATOR -> metricsRegistry.addInvalidSeparatorError();
        }
    }
}
