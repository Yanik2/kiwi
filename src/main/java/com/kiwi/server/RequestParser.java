package com.kiwi.server;

import static com.kiwi.exception.protocol.ProtocolErrorCode.HEADER_LEN_TOO_LONG;
import static com.kiwi.exception.protocol.ProtocolErrorCode.INVALID_SEPARATOR;
import static com.kiwi.exception.protocol.ProtocolErrorCode.KEY_TOO_LONG;
import static com.kiwi.exception.protocol.ProtocolErrorCode.METHOD_TOO_LONG;
import static com.kiwi.exception.protocol.ProtocolErrorCode.NON_DIGIT_IN_LENGTH;
import static com.kiwi.exception.protocol.ProtocolErrorCode.UNEXPECTED_EOF;
import static com.kiwi.exception.protocol.ProtocolErrorCode.UNKNOWN_METHOD;
import static com.kiwi.exception.protocol.ProtocolErrorCode.VALUE_TOO_LONG;
import static com.kiwi.server.Method.SET;
import static com.kiwi.server.util.ServerConstants.SEPARATOR;

import com.kiwi.server.dto.TCPRequest;
import com.kiwi.exception.protocol.ProtocolException;
import com.kiwi.persistent.dto.Key;
import com.kiwi.persistent.dto.Value;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

public class RequestParser {
    private static final Logger log = Logger.getLogger(RequestParser.class.getName());

    private static final int MAX_HEADER_KEY_LENGTH = 4;
    private static final int MAX_KEY_LENGTH = 4096;
    private static final int MAX_HEADER_VAL_LEN = 8;
    private static final int MAX_VAL_LEN = 10485760;
    private static final int MAX_HEADER_METHOD_LEN = 1;
    private static final int MAX_METHOD_LEN = 4;

    public TCPRequest parse(InputStreamWrapper is) {
        final var method = getMethod(is);

        if (method.isKeyless()) {
            return new TCPRequest(method);
        }

        final var key = getKey(is);
        if (SET.equals(method)) {
            return new TCPRequest(method, key, getValue(is));
        } else {
            return new TCPRequest(method, key);
        }
    }

    private Value getValue(InputStreamWrapper is) {
        final int valueLen = getLength(is, MAX_HEADER_VAL_LEN);
        if (valueLen > MAX_VAL_LEN) {
            log.severe("Value length bigger than allowed 10MB: " + valueLen);
            throw new ProtocolException("Value length bigger than allowed 10MB: " + valueLen,
                VALUE_TOO_LONG);
        }

        return new Value(getBytesByLength(is, valueLen));
    }

    private Key getKey(InputStreamWrapper is) {
        final int keyLen = getLength(is, MAX_HEADER_KEY_LENGTH);
        if (keyLen > MAX_KEY_LENGTH) {
            log.severe("Key length bigger than allowed 4KB: " + keyLen);
            throw new ProtocolException("Key length bigger than allowed 4KB: " + keyLen,
                KEY_TOO_LONG);
        }

        return new Key(getBytesByLength(is, keyLen));
    }

    private byte[] getBytesByLength(InputStreamWrapper is, int length) {
        final var bytes = is.readNBytes(length);
        validateSeparatorInPlace(is);
        return bytes;
    }

    private int getLength(InputStreamWrapper is, int headerLength) {
        int len = 0;
        int current;
        int counter = 0;

       while ((current = is.read()) != 13) {
           if (counter >= headerLength) {
               log.severe("Header length is too long");
               throw new ProtocolException("Header for length is too long", HEADER_LEN_TOO_LONG);
           }

           if (current >= 48 && current <= 57) {
               len = (len * 10) + (current - 48);
           } else {
               if (current == -1) {
                   log.severe("Unexpected EOF on parsing length header");
                   throw new ProtocolException("Unexpected EOF on parsing length header",
                       UNEXPECTED_EOF);
               } else {
                   log.severe("Value for single byte for length header is out of range 0-9");
                   throw new ProtocolException("Value for single byte for length header is out of range 0-9",
                       NON_DIGIT_IN_LENGTH);
               }
           }
           counter++;
       }

       if (is.read() != 10) {
           log.severe("Unexpected exception on parsing request, separator validation");
           throw new ProtocolException("Unexpected exception on parsing request, " +
               "separator validation", INVALID_SEPARATOR);
       }

       return len;
    }

    private Method getMethod(InputStreamWrapper is) {
        final var methodLen = getLength(is, MAX_HEADER_METHOD_LEN);
        if (methodLen > MAX_METHOD_LEN) {
            log.severe("Method length bigger than allowed 4 symbols: " + methodLen);
            throw new ProtocolException("Method length bigger than allowed 4 symbols: " + methodLen,
                METHOD_TOO_LONG);
        }

        final var bytes = getBytesByLength(is, methodLen);
        final var methodName = new String(bytes, StandardCharsets.UTF_8);
        try {
            return Method.valueOf(methodName);
        } catch (Exception ex) {
            log.warning("Unknown method name: " + methodName);
            throw new ProtocolException("Unknown method name: " + methodName, UNKNOWN_METHOD);
        }
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
