package com.kiwi.jvm.jfr.event;

import jdk.jfr.Event;

public class KiwiExecutorQueue implements KiwiEvent {
    private final Event event;
    private final boolean isJfrEnabled;

    public KiwiExecutorQueue(Event event, boolean isJfrEnabled) {
        this.event = event;
        this.isJfrEnabled = isJfrEnabled;
    }

    public void onEvent(String executorName, int queueSize, int queueCap, int activeWorkers, int maxWorkers,
                           int rejectedTotal, boolean backpressureActive) {
        if (isJfrEnabled) {
            event.set(0, executorName);
            event.set(1, queueSize);
            event.set(2, queueCap);
            event.set(3, activeWorkers);
            event.set(4, maxWorkers);
            event.set(5, rejectedTotal);
            event.set(6, backpressureActive);
            event.commit();
        }
    }

}
