package com.kiwi.jvm.provider;

import com.kiwi.jvm.JvmInfoSnapshot;

import java.util.Collections;

public class JvmInfoNoOpProvider implements JvmInfoProvider {
    @Override
    public JvmInfoSnapshot getInfo() {
        // temporary solution, until all logic in jvm module will be implemented
        // empty map in case if jvm info disabled will result in absence jvm info in INF method response
        // will be refactored in the end of phase-j
        return new JvmInfoSnapshot(Collections.emptyMap());
    }
}
