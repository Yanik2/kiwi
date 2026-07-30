package com.kiwi.jvm.jfr.event.provider;

import com.kiwi.jvm.jfr.event.KiwiEvent;
import com.kiwi.jvm.jfr.event.KiwiExecutorQueue;
import jdk.jfr.EventFactory;

public class KiwiExecutorQueueEventProvider implements KiwiEventProvider {
    private final EventFactory eventFactory;

    public KiwiExecutorQueueEventProvider(EventFactory eventFactory) {
        this.eventFactory = eventFactory;
    }

    @Override
    public KiwiEvent getEvent(boolean isJfrEnabled) {
        return new KiwiExecutorQueue(eventFactory.newEvent(), isJfrEnabled);
    }
}
