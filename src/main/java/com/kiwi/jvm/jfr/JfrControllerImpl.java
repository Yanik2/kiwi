package com.kiwi.jvm.jfr;

import jdk.jfr.Recording;
import jdk.jfr.RecordingState;

public class JfrControllerImpl implements JfrController {
    private final Recording recording;

    public JfrControllerImpl(Recording recording) {
        this.recording = recording;
    }

    public void start() {
        recording.start();
    }

    public void stop() {
        recording.stop();
        recording.close();
    }

    public boolean isRunning() {
        return RecordingState.RUNNING.equals(recording.getState());
    }

    public String getName() {
        return recording.getName();
    }

    public long getRecordingId() {
        return recording.getId();
    }

    public String getDestination() {
        return recording.getDestination().toString();
    }

    public long getMaxAgeSeconds() {
        return recording.getMaxAge().toSeconds();
    }

    public long getMaxSizeBytes() {
        return recording.getMaxSize();
    }

    @Override
    public boolean enabled() {
        // currently hardcode, because no other options, will be changed to dynamic in future features
        return true;
    }
}
