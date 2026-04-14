package com.kiwi.tests.utils;

import static com.kiwi.tests.utils.TestConstants.flags;

public class TestUtils {
    public static byte[] getKeyLen(String key) {
        final var keyLen = key.length();
        final byte[] keyLenBytes = new byte[2];

        keyLenBytes[0] = (byte) (keyLen >> 8);
        keyLenBytes[1] = (byte) keyLen;

        return keyLenBytes;
    }

    public static byte[] getExitRequest() {
        final var result = new byte[10];

        result[0] = flags;
        result[1] = 3;
        result[2] = 0;
        result[3] = 0;
        result[4] = 0;
        result[5] = 0;
        result[6] = 0;
        result[7] = 0;
        result[8] = 13;
        result[9] = 10;
        return result;
    }
}
