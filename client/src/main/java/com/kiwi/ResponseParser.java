package com.kiwi;

import java.io.IOException;
import java.io.InputStream;

public class ResponseParser {

    public String parse(InputStream is) throws IOException {
        final var firstByte = is.read();

        if (firstByte != 43) {
            return "error in response";
        }

        is.readNBytes(4);

        int b;
        int len = 0;
        while ((b = is.read()) != 13) {
            len *= 10;
            len += b - 48;
        }

        is.read();

        if (len == 0) {
            return "";
        } else {
            final var responseBytes = is.readNBytes(len);
            final var response = new String(responseBytes);
            is.readNBytes(2);
            return response;
        }
    }
}
