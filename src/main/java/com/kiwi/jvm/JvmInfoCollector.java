package com.kiwi.jvm;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadMXBean;
import java.util.Collections;
import java.util.HashMap;

import static com.kiwi.jvm.util.Constants.AVAILABLE_PROCESSORS;
import static com.kiwi.jvm.util.Constants.DAEMON_THREAD_COUNT;
import static com.kiwi.jvm.util.Constants.GC_COLLECTION_COUNT;
import static com.kiwi.jvm.util.Constants.GC_COLLECTION_TIME_MS;
import static com.kiwi.jvm.util.Constants.HEAP_COMMITED_BYTES;
import static com.kiwi.jvm.util.Constants.HEAP_MAX_BYTES;
import static com.kiwi.jvm.util.Constants.HEAP_USED_BYTES;
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

    public JvmInfoSnapshot collect() {
        final MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        final OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        final RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();
        final ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
        final var garbageCollectorBeans = ManagementFactory.getGarbageCollectorMXBeans();

        final var values = new HashMap<String, Object>();
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

        return new JvmInfoSnapshot(Collections.unmodifiableMap(values));
    }
}
