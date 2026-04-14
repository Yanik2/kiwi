package com.kiwi.tests.method;

import com.kiwi.tests.utils.TestException;

import java.net.Socket;
import java.util.Random;

import static com.kiwi.tests.utils.ResponseParser.parse;
import static com.kiwi.tests.utils.TestConstants.flags;
import static com.kiwi.tests.utils.TestConstants.separator;
import static com.kiwi.tests.utils.TestConstants.set;
import static com.kiwi.tests.utils.TestConstants.testValue;
import static com.kiwi.tests.utils.TestConstants.valueLen;
import static com.kiwi.tests.utils.TestConstants.zeroValueLen;
import static com.kiwi.tests.utils.TestUtils.getExitRequest;
import static com.kiwi.tests.utils.TestUtils.getKeyLen;

public class ExistsTest {
    private static final byte method = 11;

    void test() throws Exception {
        final var key = new Random().nextInt() + "exists-key";
        final var socket = new Socket("localhost", 8090);
        final var is = socket.getInputStream();
        final var os = socket.getOutputStream();
        final var keyLen = getKeyLen(key);
        final var keyBytes = key.getBytes();

        os.write(flags);
        os.write(method);
        os.write(keyLen);
        os.write(zeroValueLen);
        os.write(keyBytes);
        os.write(separator);
        os.flush();

        var response = parse(is).getBytes();
        if (response[0] != 0) {
            throw new TestException("Response in exists method is not valid. Expected: 0. Actual: " + response[0]);
        }

        os.write(flags);
        os.write(set);
        os.write(keyLen);
        os.write(valueLen);
        os.write(keyBytes);
        os.write(testValue);
        os.write(separator);
        os.flush();

        parse(is);

        os.write(flags);
        os.write(method);
        os.write(keyLen);
        os.write(zeroValueLen);
        os.write(keyBytes);
        os.write(separator);
        os.flush();

        response = parse(is).getBytes();
        if (response[0] != 1) {
            throw new TestException("Response in exists method is not valid. Expected: 1. Actual: " + response[0]);
        }

        os.write(getExitRequest());
        os.flush();
        parse(is);
        socket.close();

        System.out.println("Exists test is successful");
    }

}
