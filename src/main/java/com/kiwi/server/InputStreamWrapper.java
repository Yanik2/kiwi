package com.kiwi.server;

import com.kiwi.exception.UncheckedIOException;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;

public class InputStreamWrapper {
    private static final Logger log = Logger.getLogger(InputStreamWrapper.class.getName());

    private final InputStream inputStream;
    private int counter = 0;

    public InputStreamWrapper(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    public int read() {
        try {
            final int b = inputStream.read();
            if (b != -1) {
                counter++;
            }
            return b;
        } catch (IOException e) {
            log.severe("Unexpected exception during read input stream" + e.getMessage());
            throw new UncheckedIOException("Unexpected exception during read input stream");
        }
    }

    public byte[] readNBytes(int len) {
        try {
            final byte[] bs = inputStream.readNBytes(len);
            counter += bs.length;
            return bs;
        } catch (IOException e) {
            log.severe("Unexpected exception during read input stream" + e.getMessage());
            throw new UncheckedIOException("Unexpected exception during read input stream");
        }
    }

    public int getCounter() {
        return counter;
    }
}
