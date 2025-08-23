package com.kiwi.server;

import static com.kiwi.server.Method.EXT;
import static com.kiwi.server.Method.SET;
import static com.kiwi.server.Method.UNKNOWN;
import static com.kiwi.server.util.ServerConstants.SEPARATOR;

import com.kiwi.dto.TCPRequest;
import com.kiwi.exception.ProtocolException;
import com.kiwi.persistent.dto.Key;
import com.kiwi.persistent.dto.Value;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

public class RequestParser {
    private static final Logger log = Logger.getLogger(RequestParser.class.getName());

    private static final int MAX_HEADER_KEY_LENGTH = 4;
    private static final int MAX_KEY_LENGTH = 4096;
    private static final int MAX_HEADER_VAL_LEN = 8;
    private static final int MAX_VAL_LEN = 10485760;

    public TCPRequest parse(InputStream is) {
        final var method = getMethod(is);

        if (EXT.equals(method) || UNKNOWN.equals(method)) {
            return new TCPRequest(method);
        }

        if (SET.equals(method)) {
            return new TCPRequest(method, getKey(is), getValue(is));
        } else {
            return new TCPRequest(method, getKey(is));
        }
    }

    private Value getValue(InputStream is) {
        final int valueLen = getLength(is, MAX_HEADER_VAL_LEN);
        if (valueLen > MAX_VAL_LEN) {
            log.severe("Value length bigger than allowed 10MB: " + valueLen);
            throw new ProtocolException("Value length bigger than allowed 10MB: " + valueLen);
        }

        return new Value(getBytesByLength(is, valueLen));
    }

    private Key getKey(InputStream is) {
        final int keyLen = getLength(is, MAX_HEADER_KEY_LENGTH);
        if (keyLen > MAX_KEY_LENGTH) {
            log.severe("Key length bigger than allowed 4KB: " + keyLen);
            throw new ProtocolException("Key length bigger than allowed 4KB: " + keyLen);
        }

        return new Key(getBytesByLength(is, keyLen));
    }

    private byte[] getBytesByLength(InputStream is, int length) {
        final byte[] buf = new byte[length];

        for (int i = 0; i < length; i++) {
            final byte b = readByte(is);
            if (b != -1) {
                buf[i] = b;
            } else {
                log.severe("Unexpected EOF on parsing bytes by length");
                throw new ProtocolException("Unexpected EOF on parsing bytes by length");
            }
        }

        validateSeparatorInPlace(is);
        return buf;
    }

    private int getLength(InputStream is, int headerLength) {
        int len = 0;
        int current;
        int counter = 0;

       while ((current = readByte(is)) != 13) {
           if (counter >= headerLength) {
               log.severe("Header length is too long");
               throw new ProtocolException("Header for length is too long");
           }

           if (current >= 48 && current <= 57) {
               len = (len * 10) + (current - 48);
           } else {
               if (current == -1) {
                   log.severe("Unexpected EOF on parsing length header");
                   throw new ProtocolException("Unexpected EOF on parsing length header");
               } else {
                   log.severe("Value for single byte for length header is out of range 0-9");
                   throw new ProtocolException("Value for singe byte for length header is out of range 0-9");
               }
           }
           counter++;
       }

       if (readByte(is) != 10) {
           log.severe("Unexpected exception on parsing request, separator validation");
           throw new ProtocolException("Unexpected exception on parsing request, " +
               "separator validation");
       }

       return len;
    }

    private Method getMethod(InputStream is) {
        final byte[] methodBytes = new byte[3];

        for (int i = 0; i < 3; i++) {
            final byte b = readByte(is);
            if (b == -1) {
                log.severe("Unexpected EOF");
                throw new ProtocolException("Unexpected EOF");
            } else {
                methodBytes[i] = b;
            }
        }
        validateSeparatorInPlace(is);
        return getMethod(new String(methodBytes, StandardCharsets.UTF_8));
    }

    private Method getMethod(String str) {
        try {
            return Method.valueOf(str);
        } catch (Exception ex) {
            log.warning("Unknown method name: " + str);
            return Method.UNKNOWN;
        }
    }

    private byte readByte(InputStream is) {
        try {
            return (byte) is.read();
        } catch (Exception ex) {
            log.severe("Unexpected exception on reading input stream: " + ex.getMessage());
            throw new ProtocolException("Unexpected exception on reading input stream");
        }
    }

    private void validateSeparatorInPlace(InputStream is) {
        try {
            final byte[] separator = is.readNBytes(2);
            if (separator[0] != SEPARATOR[0] || separator[1] != SEPARATOR[1]) {
                log.severe("Separator does not validate, protocol exception");
                throw new ProtocolException("Separator does not validate");
            }
        } catch (Exception e) {
            log.severe("Unexpected exception on parsing request, separator validation");
            throw new ProtocolException("Unexpected exception on parsing request, " +
                "separator validation");
        }
    }
}
