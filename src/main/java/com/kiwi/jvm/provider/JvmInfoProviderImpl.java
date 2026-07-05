package com.kiwi.jvm.provider;

import com.kiwi.jvm.JvmInfoCollector;
import com.kiwi.jvm.JvmInfoSnapshot;

public class JvmInfoProviderImpl implements JvmInfoProvider {
    private final JvmInfoCollector jvmInfoCollector;

    public JvmInfoProviderImpl(JvmInfoCollector jvmInfoCollector) {
        this.jvmInfoCollector = jvmInfoCollector;
    }

    public JvmInfoSnapshot getInfo() {
        return jvmInfoCollector.collect();
    }
}
