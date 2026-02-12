package com.kiwi.server;

import com.kiwi.exception.protocol.ProtocolErrorCode;
import com.kiwi.exception.protocol.ProtocolException;
import com.kiwi.server.dto.ParsedRequest;

import java.util.logging.Logger;

import static com.kiwi.exception.protocol.ProtocolErrorCode.*;
import static com.kiwi.server.util.ServerConstants.SEPARATOR;

public class BinaryRequestParser {
    private static final Logger log = Logger.getLogger(BinaryRequestParser.class.getName());

    private static final int KEY_HEADER_LEN = 2;
    private static final int VALUE_HEADER_LEN = 4;
    private static final int MAX_KEY_LENGTH = 4096;
    private static final int MAX_VALUE_LENGTH = 10485760;

    public ParsedRequest parse(InputStreamWrapper is) {
        final var flags = is.read();
        final var method = getMethod(is.read());

        final var keyLen = getLength(is, KEY_HEADER_LEN);
        if (keyLen > MAX_KEY_LENGTH) {
            log.severe("Key length bigger than allowed 4KB: " + keyLen);
            throw new ProtocolException("Key length bigger than allowed 4KB: " + keyLen,
                    KEY_TOO_LONG);
        }
        final var valueLen = getLength(is, VALUE_HEADER_LEN);
        if (valueLen < 0) {
            log.severe("Invalid value header");
            throw new ProtocolException("Invalid value header", INVALID_HEADER);
        }
        if (valueLen > MAX_VALUE_LENGTH) {
            log.severe("Value length bigger than allowed 10MB: " + valueLen);
            throw new ProtocolException("Value length bigger than allowed 10MB: " + valueLen,
                    VALUE_TOO_LONG);
        }

        final var key = is.readNBytes(keyLen);
        final var value = is.readNBytes(valueLen);
        validateSeparatorInPlace(is);

        return new ParsedRequest(flags, method, key, value);
    }

    private Method getMethod(int methodId) {
        final var methods = Method.values();
        if (methodId >= 0 && methodId < methods.length) {
            return methods[methodId];
        } else {
            log.severe("Invalid method id:[" + methodId + "]");
            throw new ProtocolException("Invalid method id", ProtocolErrorCode.UNKNOWN_METHOD);
        }
    }
    private int getLength(InputStreamWrapper is, int headerLength) {
        int len = 0;
        for (int i = 0; i < headerLength; i++) {
            final var b = is.read();
            len = len << 8;
            len = len | b;
        }

        return len;
    }

    private void validateSeparatorInPlace(InputStreamWrapper is) {
        try {
            final var firstByte = is.read();
            final var secondByte = is.read();

            if (SEPARATOR[0] != firstByte || SEPARATOR[1] != secondByte) {
                log.severe("Separator does not validate, protocol exception");
                throw new ProtocolException("Separator does not validate", INVALID_SEPARATOR);
            }
        } catch (Exception e) {
            log.severe("Unexpected exception on parsing request, separator validation: "
                    + e.getMessage());
            throw new ProtocolException("Unexpected exception on parsing request, " +
                    "separator validation", INVALID_SEPARATOR);
        }
    }
}
