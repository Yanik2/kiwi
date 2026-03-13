package com.kiwi.server.buffer;

import com.kiwi.exception.protocol.ProtocolErrorCode;
import com.kiwi.exception.protocol.ProtocolException;
import com.kiwi.server.context.ConnectionContext;

import java.io.InputStream;
import java.util.logging.Logger;

import static com.kiwi.config.properties.Properties.BUFFER_EXPAND_RATIO;
import static com.kiwi.config.properties.Properties.BUFFER_INITIAL_CAP;
import static com.kiwi.config.properties.Properties.BUFFER_MAX_CAP;

public class ReadBuffer {
    private static final Logger log = Logger.getLogger(ReadBuffer.class.getName());

    private byte[] buf = new byte[BUFFER_INITIAL_CAP];

    private int readPos = 0;
    private int writePos = 0;
    private int readBytes = 0;

    public int fill(InputStream is, ConnectionContext context) {
        if (!isFull()) {
            return fillBuffer(is, context);
        } else {
            log.severe("Trying to fill full buffer, connection id: [" + context.connectionId() + "]");
            throw new ProtocolException("Trying to fill full buffer", ProtocolErrorCode.BUFFER_ERROR);
        }
    }

    private int fillBuffer(InputStream is, ConnectionContext context) {
        if (writePos == buf.length) {
            expandBuffer();
        }

        try {
            final int bytesRead = is.read(buf, writePos, buf.length - writePos);
            if (bytesRead > 0) {
                writePos += bytesRead;
                readBytes += bytesRead;
                shrinkBuffer();
            }
            return bytesRead;
        } catch (Exception ex) {
            if (context.isClosed()) {
                return 0;
            } else {
                log.severe("Unexpected error in read buffer on reading request: " + ex.getMessage() +
                        ". Connection id: [" + context.connectionId() + "]");
                throw new ProtocolException("Unexpected exception in read buffer", ProtocolErrorCode.BUFFER_ERROR);
            }
        }
    }

    private void shrinkBuffer() {
        final var newSize = Math.max((writePos - readPos) * 2, BUFFER_INITIAL_CAP);
        if (newSize <= buf.length / 2) {
            final var newBuf = new byte[newSize];
            for (int i = 0; i < writePos - readPos; i++) {
                newBuf[i] = buf[readPos + i];
            }

            writePos -= readPos;
            readPos = 0;
            this.buf = newBuf;
        }
    }

    private void expandBuffer() {
        compact();
        if (writePos < buf.length - 1) {
            return;
        }
        final var newSize = Math.min(buf.length * BUFFER_EXPAND_RATIO, BUFFER_MAX_CAP);
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
        return readPos == 0 && writePos == BUFFER_MAX_CAP;
    }
}