package com.kiwi.server;

import static com.kiwi.util.Constants.EXIT;
import static com.kiwi.util.Constants.SET;

import com.kiwi.dto.TCPRequest;
import java.io.IOException;
import java.io.InputStream;

public class RequestParser {

    public TCPRequest parse(InputStream is) throws IOException {
        final var method = new String(is.readNBytes(3));
        is.readNBytes(2);

        if (EXIT.equals(method)) {
            return new TCPRequest(method);
        }

        final var keyLen = getLength(is);
        is.readNBytes(1);
        final var key = new String(is.readNBytes(keyLen));
        is.readNBytes(2);

        if (SET.equals(method)) {
            final var valueLen = getLength(is);
            is.readNBytes(1);
            final var value = is.readNBytes(valueLen);
            is.readNBytes(2);

            return new TCPRequest(method, key, value);
        } else {
            return new TCPRequest(method, key);
        }
    }

    private int getLength(InputStream is) throws IOException {
        int len = 0;
        int current;

        while ((current = is.read()) != 13) {
            len = (len * 10) + (current - 48);
        }

        return len;
    }
}
