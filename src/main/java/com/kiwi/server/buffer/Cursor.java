package com.kiwi.server.buffer;

public class Cursor {
    private final ReadBuffer readBuffer;
    private int currentPosition;

    public Cursor(ReadBuffer readBuffer) {
        this.readBuffer = readBuffer;
        this.currentPosition = readBuffer.getReadPos();
    }

    public int bytesAvailable() {
        return readBuffer.getWritePos() - currentPosition;
    }

    public byte pop() {
        return readBuffer.get(currentPosition++);
    }

    public void reset() {
        this.currentPosition = readBuffer.getReadPos();
    }

    public byte[] getBytes(byte[] arr, int len) {
        final var bytes = readBuffer.getRange(arr, currentPosition, len);
        this.currentPosition += len;
        return bytes;
    }

    public void advance() {
        readBuffer.advance(this.currentPosition);
    }

    public void toEnd() {
        this.currentPosition = readBuffer.getWritePos();
    }
}
