package com.kiwi.jvm.factory;


import com.kiwi.jvm.provider.JvmInfoProvider;

public record JvmModuleContainer(
        JvmInfoProvider jvmInfoProvider
) {
}
