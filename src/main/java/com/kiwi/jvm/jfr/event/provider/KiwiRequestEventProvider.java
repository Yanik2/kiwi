package com.kiwi.jvm.jfr.event.provider;

import com.kiwi.jvm.jfr.event.KiwiEvent;
import com.kiwi.jvm.jfr.event.KiwiRequest;
import jdk.jfr.EventFactory;

public class KiwiRequestEventProvider implements KiwiEventProvider {
    private final EventFactory eventFactory;

    public KiwiRequestEventProvider(EventFactory eventFactory) {
        this.eventFactory = eventFactory;
    }

    @Override
    public KiwiEvent getEvent(boolean isJfrEnabled) {
        return new KiwiRequest(eventFactory.newEvent(), isJfrEnabled);
    }
}
