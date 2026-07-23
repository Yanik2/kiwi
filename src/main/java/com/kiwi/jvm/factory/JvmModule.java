
package com.kiwi.jvm.factory;

import com.kiwi.config.domain.JvmConfig;
import com.kiwi.jvm.JvmInfoCollector;
import com.kiwi.jvm.jfr.JfrController;
import com.kiwi.jvm.jfr.JfrControllerImpl;
import com.kiwi.jvm.jfr.NoOpJfrController;
import com.kiwi.jvm.provider.JvmInfoProviderImpl;
import com.kiwi.log.KiwiLogger;
import com.kiwi.log.KiwiLoggerFactory;
import jdk.jfr.Recording;

import java.nio.file.Paths;
import java.time.Duration;

public class JvmModule {
    private static final KiwiLogger log = KiwiLoggerFactory.getLogger(JvmModule.class.getName());

    public static JvmModuleContainer create(JvmConfig jvmConfig) {
        final JfrController jfrController;
        if (jvmConfig.jfrEnabled()) {
            jfrController = initJavaFlightRecording(jvmConfig);
        } else {
            jfrController = new NoOpJfrController();
        }

        jfrController.start();
        final var jvmInfoCollector = new JvmInfoCollector(jfrController, jvmConfig.jvmInfoEnabled());
        return new JvmModuleContainer(new JvmInfoProviderImpl(jvmInfoCollector), jfrController);
    }

    private static JfrController initJavaFlightRecording(JvmConfig jvmConfig) {
        try {
            final var recording = new Recording();
            recording.setDestination(Paths.get(jvmConfig.jfrDir()));
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
}
