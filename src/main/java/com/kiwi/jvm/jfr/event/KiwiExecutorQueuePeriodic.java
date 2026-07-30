package com.kiwi.jvm.jfr.event;

import com.kiwi.concurrency.KiwiThreadPoolExecutor;
import jdk.jfr.Category;
import jdk.jfr.Event;
import jdk.jfr.FlightRecorder;
import jdk.jfr.Label;
import jdk.jfr.Name;
import jdk.jfr.Period;

@Name("com.kiwi.KiwiExecutorQueuePeriodic")
@Label("Kiwi Execution Queue Periodic")
@Category("Kiwi")
@Period("1 s")
public class KiwiExecutorQueuePeriodic extends Event {

    private String executorName;
    private int queueSize;
    private int queueCapacity;
    private int activeWorkers;
    private int maxWorkers;
    private int rejectedTotal;
    private boolean backpressureActive;

    public static void configure(KiwiThreadPoolExecutor threadPoolExecutor) {
        final Runnable hook = () -> {
            final var event = new KiwiExecutorQueuePeriodic();
            event.setExecutorName(threadPoolExecutor.getName());
            event.setQueueSize(threadPoolExecutor.queueSize());
            event.setQueueCapacity(threadPoolExecutor.queueCap());
            event.setActiveWorkers(threadPoolExecutor.activeWorkers());
            event.setMaxWorkers(threadPoolExecutor.maxWorkers());
            event.setRejectedTotal(threadPoolExecutor.rejectedTotal());
            event.setBackpressureActive(threadPoolExecutor.isBackpressureActive());

            event.commit();
        };

        FlightRecorder.addPeriodicEvent(KiwiExecutorQueuePeriodic.class, hook);
    }

    public void setExecutorName(String executorName) {
        this.executorName = executorName;
    }

    public void setQueueSize(int queueSize) {
        this.queueSize = queueSize;
    }

    public void setQueueCapacity(int queueCapacity) {
        this.queueCapacity = queueCapacity;
    }

    public void setActiveWorkers(int activeWorkers) {
        this.activeWorkers = activeWorkers;
    }

    public void setMaxWorkers(int maxWorkers) {
        this.maxWorkers = maxWorkers;
    }

    public void setRejectedTotal(int rejectedTotal) {
        this.rejectedTotal = rejectedTotal;
    }

    public void setBackpressureActive(boolean backpressureActive) {
        this.backpressureActive = backpressureActive;
    }
}
