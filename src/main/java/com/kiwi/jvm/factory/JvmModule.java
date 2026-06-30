package com.kiwi.jvm.factory;

import com.kiwi.jvm.JvmInfoCollector;
import com.kiwi.jvm.JvmInfoProvider;

public class JvmModule {
    public static JvmModuleContainer create() {
        return new JvmModuleContainer(new JvmInfoProvider(new JvmInfoCollector()));
    }
}
