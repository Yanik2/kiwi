package com.kiwi.jvm.jfr;

import jdk.jfr.Recording;
import jdk.jfr.RecordingState;

import java.io.IOException;

public class JfrControllerImpl implements JfrController {
    private final Recording recording;

    public JfrControllerImpl(Recording recording) {
        this.recording = recording;
    }

    public void start() {
        if (RecordingState.NEW.equals(recording.getState())) {
            recording.start();
        }
    }

    public void stop() {
        if (RecordingState.RUNNING.equals(recording.getState())) {
            recording.stop();
        }
        dump();
        if (!RecordingState.CLOSED.equals(recording.getState())) {
            recording.close();
        }
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

    private void dump() {
        if (RecordingState.RUNNING.equals(recording.getState())
                || RecordingState.STOPPED.equals(recording.getState())) {
            try {
                recording.dump(recording.getDestination());
            } catch (IOException e) {
                // ignore because check was performed (this is single threaded class)
            }
        }
    }
}
