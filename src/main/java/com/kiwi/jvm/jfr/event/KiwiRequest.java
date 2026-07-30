package com.kiwi.jvm.jfr.event;

import com.kiwi.server.request.Method;
import com.kiwi.server.response.model.TCPResponseResult;
import jdk.jfr.Event;

public class KiwiRequest implements KiwiEvent {
    private final Event event;
    // currently this boolean is final and will not change in runtime if jfr enabled via user command
    // will be implemented dynamically if needed in later PRs
    private final boolean isJfrEnabled;

    private long beforeExecutionWait;
    private long executionDuration;
    private long afterExecutionWait;

    private long startTime;

    public KiwiRequest(Event event, boolean isJfrEnabled) {
        this.event = event;
        this.isJfrEnabled = isJfrEnabled;
    }

    public void waitForExecution() {
        if (isJfrEnabled) {
            event.begin();
            this.startTime = System.nanoTime();
        }
    }

    public void startExecution() {
        if (isJfrEnabled) {
            this.beforeExecutionWait = System.nanoTime() - startTime;
            this.startTime = System.nanoTime();
        }
    }

    public void waitForComplete() {
        if (isJfrEnabled) {
            this.executionDuration = System.nanoTime() - startTime;
            this.startTime = System.nanoTime();
        }
    }

    public void complete(int requestId, Method method, long connectionId, TCPResponseResult result) {
        if (isJfrEnabled) {
            this.afterExecutionWait = System.nanoTime() - startTime;
            event.set(0, this.beforeExecutionWait / 1000000.0);
            event.set(1, this.afterExecutionWait / 1000000.0);
            event.set(2, this.executionDuration / 1000000.0);
            event.set(3, requestId);
            event.set(4, connectionId);
            event.set(5, method.toString());
            event.set(6, result.toString());
            event.end();
            event.commit();
        }
    }
}
