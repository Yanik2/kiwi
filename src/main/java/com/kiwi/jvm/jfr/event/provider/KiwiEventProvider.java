package com.kiwi.jvm.jfr.event.provider;

import com.kiwi.jvm.jfr.event.KiwiEvent;

public interface KiwiEventProvider {
    KiwiEvent getEvent(boolean isJfrEnabled);
}
