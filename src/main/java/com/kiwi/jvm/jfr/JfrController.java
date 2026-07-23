package com.kiwi.jvm.jfr;

import com.kiwi.exception.jvm.JfrIllegalStateException;

public interface JfrController {
    void start();
    void stop();
    boolean enabled();

    default boolean isRunning() {
        return false;
    }

    default String getName() {
        throw new JfrIllegalStateException("Unsupported operation for current JFR Controller");
    }

    default long getRecordingId() {
        throw new JfrIllegalStateException("Unsupported operation for current JFR Controller");
    }

    default String getDestination() {
        throw new JfrIllegalStateException("Unsupported operation for current JFR Controller");
    }

    default long getMaxAgeSeconds() {
        throw new JfrIllegalStateException("Unsupported operation for current JFR Controller");
    }

    default long getMaxSizeBytes() {
        throw new JfrIllegalStateException("Unsupported operation for current JFR Controller");
    }
}
