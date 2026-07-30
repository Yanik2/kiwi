package com.kiwi.jvm.jfr;

import com.kiwi.config.domain.JvmConfig;
import com.kiwi.log.KiwiLogger;
import com.kiwi.log.KiwiLoggerFactory;
import jdk.jfr.Configuration;
import jdk.jfr.Recording;
import jdk.jfr.RecordingState;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

import static com.kiwi.jvm.util.Constants.JFR_FILE_TEMPLATE;

public class JfrControllerImpl implements JfrController {
    private static final KiwiLogger log = KiwiLoggerFactory.getLogger(JfrControllerImpl.class.getName());

    private final Recording recording;

    private JfrControllerImpl(Recording recording) {
        this.recording = recording;
    }

    public static JfrController create(JvmConfig jvmConfig) {
        try {
            final var configuration = Configuration.getConfiguration("profile");
            final var recording = new Recording(configuration);
            final var filePath = Path.of(jvmConfig.jfrDir());
            if (Files.notExists(filePath)) {
                Files.createDirectory(filePath);
            }
            final var pid = ProcessHandle.current().pid();
            final var destination = jvmConfig.jfrDir() + "/"
                    + JFR_FILE_TEMPLATE.formatted(pid, System.currentTimeMillis());
            recording.setDestination(Path.of(destination));
            recording.setMaxAge(Duration.ofSeconds(jvmConfig.jfrMaxAgeSeconds()));
            recording.setMaxSize(jvmConfig.jfrMaxSizeBytes());
            recording.setName("kiwi-main");
            recording.setToDisk(true);
            recording.setDumpOnExit(true);
            return new JfrControllerImpl(recording);
        } catch (Exception ex) {
            log.warn("Error during initializing Java Flight Recording", ex.getMessage());
            return new NoOpJfrController();
        }
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
                log.warn("Unexpected error during dump java flight recording record", e.getMessage());
                // ignore because check was performed (this is single threaded class)
            }
        }
    }
}
