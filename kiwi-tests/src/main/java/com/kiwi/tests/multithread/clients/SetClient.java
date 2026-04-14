package com.kiwi.tests.multithread.clients;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static com.kiwi.tests.utils.TestConstants.separator;
import static com.kiwi.tests.utils.TestConstants.testValue;
import static com.kiwi.tests.utils.TestConstants.valueLen;
import static com.kiwi.tests.utils.TestUtils.getKeyLen;

public class SetClient implements Client {
    private static final byte method = 1;

    private final List<String> keys;
    private final List<Integer> keyIndexes;

    private final AtomicInteger keyIndexesCursor = new AtomicInteger();

    public SetClient(List<String> keys, List<Integer> keyIndexes) {
        this.keys = keys;
        this.keyIndexes = keyIndexes;
    }

    @Override
    public void execute(OutputStream os) throws IOException {
        final var cursor = keyIndexesCursor.getAndIncrement();
        final var currentIndex = keyIndexes.get(cursor);
        final var currentKey = keys.get(currentIndex);

        os.write(method);
        os.flush();
        os.write(new byte[]{0, 1});
        os.write(getKeyLen(currentKey));
        os.flush();
        os.write(valueLen);
        os.flush();
        os.write(currentKey.getBytes());
        os.flush();
        os.write(testValue);
        os.flush();
        os.write(separator);
        os.flush();
    }
}
