package com.kiwi.concurrency.task;

public interface Task {
    void execute();
    void reject();
    int getTimeout();
}
