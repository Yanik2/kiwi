package com.kiwi.jvm.factory;

import com.kiwi.config.domain.JvmConfig;
import com.kiwi.jvm.JvmInfoCollector;
import com.kiwi.jvm.provider.JvmInfoNoOpProvider;
import com.kiwi.jvm.provider.JvmInfoProviderImpl;

public class JvmModule {
    public static JvmModuleContainer create(JvmConfig jvmConfig) {
        return new JvmModuleContainer(
                jvmConfig.jvmInfoEnabled() ? new JvmInfoProviderImpl(new JvmInfoCollector()) : new JvmInfoNoOpProvider()
        );
    }
}
