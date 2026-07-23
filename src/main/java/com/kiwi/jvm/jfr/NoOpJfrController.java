package com.kiwi.jvm.jfr;

public class NoOpJfrController implements JfrController {
    @Override
    public void start() {
        // do nothing
    }

    @Override
    public void stop() {
        // do nothing
    }

    @Override
    public boolean enabled() {
        return false;
    }
}
