package com.kiwi.server.buffer;

import com.kiwi.exception.protocol.ProtocolErrorCode;
import com.kiwi.exception.protocol.ProtocolException;

import java.io.InputStream;
import java.util.logging.Logger;

public class ReadBuffer {
    private static final Logger log = Logger.getLogger(ReadBuffer.class.getName());

    private static final int INITIAL_CAP = 8192;
    private static final int MAX_CAP = 10489864;
    private static final int EXPAND_RATIO = 2;

    private byte[] buf = new byte[INITIAL_CAP];

    private int readPos = 0;
    private int writePos = 0;
    private int readBytes = 0;

    public void fill(InputStream is) {
        if (!isFull()) {
            fillBuffer(is);
        } else {
            log.severe("Trying to fill full buffer");
            throw new ProtocolException("Trying to fill full buffer", ProtocolErrorCode.BUFFER_ERROR);
        }
    }

    private void fillBuffer(InputStream is) {
        try {
            do {
                if (writePos == buf.length) {
                    expandBuffer();
                }
                buf[writePos++] = (byte) is.read();
                readBytes++;
            } while (!isFull() && is.available() > 0);
        } catch (Exception ex) {
            log.severe("Unexpected error in read buffer on reading request: " + ex.getMessage());
            throw new ProtocolException("Unexpected exception in read buffer", ProtocolErrorCode.BUFFER_ERROR);
        }
    }

    private void expandBuffer() {
        compact();
        if (writePos < buf.length - 1) {
            return;
        }
        final var newSize = Math.min(buf.length * EXPAND_RATIO, MAX_CAP);
        final var newBuffer = new byte[newSize];

        for (int i = 0; i < buf.length; i++) {
            newBuffer[i] = buf[i];
        }

        this.buf = newBuffer;
    }

    public void compact() {
        if (readPos > 0) {
            for (int i = 0; i < writePos - readPos; i++) {
                this.buf[i] = this.buf[readPos + i];
            }
            this.writePos = writePos - readPos;
            this.readPos = 0;
        }
    }

    public int getReadPos() {
        return readPos;
    }

    public int getWritePos() {
        return writePos;
    }

    public byte get(int index) {
        return buf[index];
    }

    public byte[] getRange(byte[] arr, int offset, int len) {
        for (int i = 0; i < len; i++) {
            arr[i] = buf[offset + i];
        }

        return arr;
    }

    public void advance(int index) {
        if (index < 0 || index > writePos) {
            throw new ProtocolException("Invalid index to advance read buffer", ProtocolErrorCode.BUFFER_ERROR);
        }
        this.readPos = index;
    }

    public int getReadBytes() {
        return readBytes;
    }

    private boolean isFull() {
        return readPos == 0 && writePos == MAX_CAP;
    }
}