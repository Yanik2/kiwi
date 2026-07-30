
package com.kiwi.jvm.factory;

import com.kiwi.config.domain.JvmConfig;
import com.kiwi.jvm.JvmInfoCollector;
import com.kiwi.jvm.jfr.JfrController;
import com.kiwi.jvm.jfr.JfrControllerImpl;
import com.kiwi.jvm.jfr.NoOpJfrController;
import com.kiwi.jvm.provider.JvmInfoProviderImpl;

public class JvmModule {

    public static JvmModuleContainer create(JvmConfig jvmConfig) {
        final JfrController jfrController;
        if (jvmConfig.jfrEnabled()) {
            jfrController = JfrControllerImpl.create(jvmConfig);
        } else {
            jfrController = new NoOpJfrController();
        }

        jfrController.start();
        final var jvmInfoCollector = new JvmInfoCollector(jfrController, jvmConfig.jvmInfoEnabled());
        JfrEventFactory.init(jvmConfig.jfrEnabled(), jvmConfig.jfrThreshold());
        return new JvmModuleContainer(new JvmInfoProviderImpl(jvmInfoCollector), jfrController,
                JfrEventFactory.getInstance());
    }

}
