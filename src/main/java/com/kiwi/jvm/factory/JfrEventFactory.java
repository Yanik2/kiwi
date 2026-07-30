package com.kiwi.jvm.factory;

import com.kiwi.concurrency.KiwiThreadPoolExecutor;
import com.kiwi.jvm.jfr.event.KiwiExecutorQueue;
import com.kiwi.jvm.jfr.event.KiwiExecutorQueuePeriodic;
import com.kiwi.jvm.jfr.event.KiwiRequest;
import com.kiwi.jvm.jfr.event.provider.KiwiEventProvider;
import com.kiwi.jvm.jfr.event.provider.KiwiExecutorQueueEventProvider;
import com.kiwi.jvm.jfr.event.provider.KiwiRequestEventProvider;
import jdk.jfr.AnnotationElement;
import jdk.jfr.Category;
import jdk.jfr.EventFactory;
import jdk.jfr.Label;
import jdk.jfr.Name;
import jdk.jfr.Threshold;
import jdk.jfr.ValueDescriptor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;


public class JfrEventFactory {
    private static JfrEventFactory instance;

    private final boolean isJfrEnabled;

    private final Map<Class<?>, KiwiEventProvider> eventStrategies;

    private JfrEventFactory(boolean isJfrEnabled, Map<Class<?>, KiwiEventProvider> eventStrategies) {
        this.isJfrEnabled = isJfrEnabled;
        this.eventStrategies = eventStrategies;
    }

    public static JfrEventFactory getInstance() {
        return instance;
    }

    static void init(boolean isJfrEnabled, int threshold) {
        final var category = new String[]{"Kiwi"};
        final var requestAnnotations = new ArrayList<AnnotationElement>();
        requestAnnotations.add(new AnnotationElement(Name.class, "com.kiwi.KiwiRequest"));
        requestAnnotations.add(new AnnotationElement(Label.class, "Kiwi Request"));
        requestAnnotations.add(new AnnotationElement(Category.class, category));
        requestAnnotations.add(new AnnotationElement(Threshold.class, threshold + " ms"));

        final var requestFields = new ArrayList<ValueDescriptor>();
        final var beforeLabel = Collections.singletonList(new AnnotationElement(Label.class, "Before Exec Wait"));
        requestFields.add(new ValueDescriptor(float.class, "beforeExecutionWait", beforeLabel));

        final var afterLabel = Collections.singletonList(new AnnotationElement(Label.class, "After Exec Wait"));
        requestFields.add(new ValueDescriptor(float.class, "afterExecutionWait", afterLabel));

        final var executionLabel = Collections.singletonList(new AnnotationElement(Label.class, "Execution Duration"));
        requestFields.add(new ValueDescriptor(float.class, "executionDuration", executionLabel));

        final var requestIdLabel = Collections.singletonList(new AnnotationElement(Label.class, "Request ID"));
        requestFields.add(new ValueDescriptor(int.class, "requestId", requestIdLabel));

        final var connectionIdLabel = Collections.singletonList(new AnnotationElement(Label.class, "Connection ID"));
        requestFields.add(new ValueDescriptor(long.class, "connectionId", connectionIdLabel));

        final var commandLabel = Collections.singletonList(new AnnotationElement(Label.class, "Command"));
        requestFields.add(new ValueDescriptor(String.class, "command", commandLabel));

        final var resultLabel = Collections.singletonList(new AnnotationElement(Label.class, "Result"));
        requestFields.add(new ValueDescriptor(String.class, "result", resultLabel));

        final var kiwiRequestFactory = EventFactory.create(requestAnnotations, requestFields);

        final var queueAnnotations = new ArrayList<AnnotationElement>();
        queueAnnotations.add(new AnnotationElement(Name.class, "com.kiwi.KiwiExecutionQueue"));
        queueAnnotations.add(new AnnotationElement(Label.class, "Kiwi Execution Queue"));
        queueAnnotations.add(new AnnotationElement(Category.class, category));

        final var queueFields = new ArrayList<ValueDescriptor>();
        final var executorNameLabel = Collections.singletonList(new AnnotationElement(Label.class, "Executor name"));
        queueFields.add(new ValueDescriptor(String.class, "executorName", executorNameLabel));

        final var queueSizeLabel = Collections.singletonList(new AnnotationElement(Label.class, "Queue size"));
        queueFields.add(new ValueDescriptor(int.class, "queueSize", queueSizeLabel));

        final var queueCapacityLabel = Collections.singletonList(new AnnotationElement(Label.class, "Queue cap"));
        queueFields.add(new ValueDescriptor(int.class, "queueCapacity", queueCapacityLabel));

        final var activeWorkersLabel = Collections.singletonList(new AnnotationElement(Label.class, "Active workers"));
        queueFields.add(new ValueDescriptor(int.class, "activeWorkers", activeWorkersLabel));

        final var maxWorkersLabel = Collections.singletonList(new AnnotationElement(Label.class, "Max workers"));
        queueFields.add(new ValueDescriptor(int.class, "maxWorkers", maxWorkersLabel));

        final var rejectedTotalLabel = Collections.singletonList(new AnnotationElement(Label.class, "Rejected total"));
        queueFields.add(new ValueDescriptor(long.class, "rejectedTotal", rejectedTotalLabel));

        final var backpressureActiveLabel = Collections.singletonList(new AnnotationElement(Label.class,
                "Backpressure active"));
        queueFields.add(new ValueDescriptor(boolean.class, "backpressureActive", backpressureActiveLabel));
        final var kiwiQueueFactory = EventFactory.create(queueAnnotations, queueFields);

        final var strategies = Map.<Class<?>, KiwiEventProvider>of(
                KiwiRequest.class, new KiwiRequestEventProvider(kiwiRequestFactory),
                KiwiExecutorQueue.class, new KiwiExecutorQueueEventProvider(kiwiQueueFactory)
        );
        instance = new JfrEventFactory(isJfrEnabled, strategies);
    }

    @SuppressWarnings("unchecked")
    public <T> T createEvent(Class<T> type) {
        return (T) eventStrategies.get(type).getEvent(isJfrEnabled);
    }

    public void createPeriodicEvent(KiwiThreadPoolExecutor threadPoolExecutor) {
        if (isJfrEnabled) {
            KiwiExecutorQueuePeriodic.configure(threadPoolExecutor);
        }
    }
}
