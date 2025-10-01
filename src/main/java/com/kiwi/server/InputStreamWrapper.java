package com.kiwi.server;

import com.kiwi.exception.UncheckedIOException;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;

public class InputStreamWrapper {
    private static final Logger log = Logger.getLogger(InputStreamWrapper.class.getName());

    private final InputStream inputStream;

    public InputStreamWrapper(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    public int read() {
        try {
            return inputStream.read();
        } catch (IOException e) {
            log.severe("Unexpected exception during read input stream" + e.getMessage());
            throw new UncheckedIOException("Unexpected exception during read input stream");
        }
    }

    public byte[] readNBytes(int len) {
        try {
            return inputStream.readNBytes(len);
        } catch (IOException e) {
            log.severe("Unexpected exception during read input stream" + e.getMessage());
            throw new UncheckedIOException("Unexpected exception during read input stream");
        }
    }
}
