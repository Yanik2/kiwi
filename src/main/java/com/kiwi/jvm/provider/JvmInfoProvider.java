package com.kiwi.jvm.provider;

import com.kiwi.jvm.JvmInfoSnapshot;

public interface JvmInfoProvider {
    JvmInfoSnapshot getInfo();
}
