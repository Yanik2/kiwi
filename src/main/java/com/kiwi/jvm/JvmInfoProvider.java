package com.kiwi.jvm;

public class JvmInfoProvider {
    private final JvmInfoCollector jvmInfoCollector;

    public JvmInfoProvider(JvmInfoCollector jvmInfoCollector) {
        this.jvmInfoCollector = jvmInfoCollector;
    }

    public JvmInfoSnapshot getJvmInfo() {
        return jvmInfoCollector.collect();
    }
}
