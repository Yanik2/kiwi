package com.kiwi.jvm;

import com.kiwi.jvm.jfr.JfrController;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadMXBean;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.kiwi.jvm.util.Constants.AVAILABLE_PROCESSORS;
import static com.kiwi.jvm.util.Constants.DAEMON_THREAD_COUNT;
import static com.kiwi.jvm.util.Constants.GC_COLLECTION_COUNT;
import static com.kiwi.jvm.util.Constants.GC_COLLECTION_TIME_MS;
import static com.kiwi.jvm.util.Constants.HEAP_COMMITED_BYTES;
import static com.kiwi.jvm.util.Constants.HEAP_MAX_BYTES;
import static com.kiwi.jvm.util.Constants.HEAP_USED_BYTES;
import static com.kiwi.jvm.util.Constants.JFR_DESTINATION;
import static com.kiwi.jvm.util.Constants.JFR_ENABLED;
import static com.kiwi.jvm.util.Constants.JFR_MAX_AGE_SECONDS;
import static com.kiwi.jvm.util.Constants.JFR_MAX_SIZE_BYTES;
import static com.kiwi.jvm.util.Constants.JFR_NAME;
import static com.kiwi.jvm.util.Constants.JFR_RECORDING_ID;
import static com.kiwi.jvm.util.Constants.JFR_RUNNING;
import static com.kiwi.jvm.util.Constants.NON_HEAP_COMMITED_BYTES;
import static com.kiwi.jvm.util.Constants.NON_HEAP_MAX_BYTES;
import static com.kiwi.jvm.util.Constants.NON_HEAP_USED_BYTES;
import static com.kiwi.jvm.util.Constants.PEAK_THREAD_COUNT;
import static com.kiwi.jvm.util.Constants.PROCESS_CPU_LOAD;
import static com.kiwi.jvm.util.Constants.START_TIME_MS;
import static com.kiwi.jvm.util.Constants.SYSTEM_CPU_LOAD;
import static com.kiwi.jvm.util.Constants.THREAD_COUNT;
import static com.kiwi.jvm.util.Constants.UPTIME_MS;

public class JvmInfoCollector {
    private final JfrController jfrController;
    private final boolean jvmEnabled;

    public JvmInfoCollector(JfrController jfrController, boolean jvmEnabled) {
        this.jfrController = jfrController;
        this.jvmEnabled = jvmEnabled;
    }

    public JvmInfoSnapshot collect() {
        final var data = new HashMap<String, Object>();
        if (jvmEnabled) {
            addJvmInfo(data);
        }

        if (jfrController.enabled()) {
            data.put(JFR_ENABLED, jfrController.enabled());
            data.put(JFR_RUNNING, jfrController.isRunning());
            data.put(JFR_RECORDING_ID, jfrController.getRecordingId());
            data.put(JFR_NAME, jfrController.getName());
            data.put(JFR_DESTINATION, jfrController.getDestination());
            data.put(JFR_MAX_AGE_SECONDS, jfrController.getMaxAgeSeconds());
            data.put(JFR_MAX_SIZE_BYTES, jfrController.getMaxSizeBytes());
        } else {
            data.put(JFR_ENABLED, false);
            data.put(JFR_RUNNING, false);
        }

        return new JvmInfoSnapshot(Collections.unmodifiableMap(data));
    }

    private void addJvmInfo(Map<String, Object> values) {
        final MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        final OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        final RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();
        final ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
        final var garbageCollectorBeans = ManagementFactory.getGarbageCollectorMXBeans();

        values.put(UPTIME_MS, runtimeBean.getUptime());
        values.put(START_TIME_MS, runtimeBean.getStartTime());
        values.put(HEAP_USED_BYTES, memoryBean.getHeapMemoryUsage().getUsed());
        values.put(HEAP_COMMITED_BYTES, memoryBean.getHeapMemoryUsage().getCommitted());
        values.put(HEAP_MAX_BYTES, memoryBean.getHeapMemoryUsage().getMax());
        values.put(NON_HEAP_USED_BYTES, memoryBean.getNonHeapMemoryUsage().getUsed());
        values.put(NON_HEAP_COMMITED_BYTES, memoryBean.getNonHeapMemoryUsage().getCommitted());
        values.put(NON_HEAP_MAX_BYTES, memoryBean.getNonHeapMemoryUsage().getMax());

        long collectionCount = 0;
        long collectionTimeMs = 0;
        for (var gcBean : garbageCollectorBeans) {
            long count = gcBean.getCollectionCount();
            if (count > 0) {
                collectionCount += count;
            }

            long time = gcBean.getCollectionTime();
            if (time > 0) {
                collectionTimeMs += time;
            }
        }

        values.put(GC_COLLECTION_COUNT, collectionCount);
        values.put(GC_COLLECTION_TIME_MS, collectionTimeMs);
        values.put(THREAD_COUNT, threadBean.getThreadCount() - threadBean.getDaemonThreadCount());
        values.put(DAEMON_THREAD_COUNT, threadBean.getDaemonThreadCount());
        values.put(PEAK_THREAD_COUNT, threadBean.getPeakThreadCount());
        values.put(AVAILABLE_PROCESSORS, osBean.getAvailableProcessors());
        values.put(PROCESS_CPU_LOAD, -1);
        values.put(SYSTEM_CPU_LOAD, osBean.getSystemLoadAverage());
    }
}
