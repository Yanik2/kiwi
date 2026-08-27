package com.kiwi.jvm.gc.report;

import com.kiwi.jvm.gc.GcLogEntry;
import com.kiwi.jvm.gc.parser.GcLogParser;

import static com.kiwi.jvm.gc.GcCycleType.UNKNOWN;
import static com.kiwi.jvm.gc.GcEntryType.GC;

public class GcReportService {
    private final GcLogParser parser;

    public GcReportService(GcLogParser parser) {
        this.parser = parser;
    }

    public void displayReport(String filename) {
        final var data = parser.parse(filename)
                .stream()
                .filter(e -> GC.equals(e.entryType()) && !UNKNOWN.equals(e.parsedMessage().cycleType())
                        && !e.tags().contains("start"))
                .toList();

        int totalCollections = 0;
        int youngCollections = 0;
        int mixedCollections = 0;
        int fullCollections = 0;

        float totalPauseMs = 0;
        float maxPauseMs = 0;

        long heapBeforeAverage = 0;
        long heapAfterAverage = 0;
        long heapBeforeMax = 0;
        long heapAfterMax = 0;
        long reclaimedTotalBytes = 0;

        for (GcLogEntry entry : data) {
            totalCollections++;
            final var message = entry.parsedMessage();

            switch (message.cycleType()) {
                case YOUNG_NORMAL -> youngCollections++;
                case YOUNG_MIXED, YOUNG_PREPARE_MIXED -> mixedCollections++;
                case FULL -> fullCollections++;
            }

            totalPauseMs += message.timeSpent() >= 0 ? message.timeSpent() : 0;

            if (message.timeSpent() > maxPauseMs) {
                maxPauseMs = message.timeSpent();
            }

            heapBeforeAverage += message.heapSizeBefore() >= 0 ? message.heapSizeBefore() : 0;
            heapAfterAverage += message.heapSizeAfter() >= 0 ? message.heapSizeAfter() : 0;

            if (message.heapSizeBefore() > heapBeforeMax) {
                heapBeforeMax = message.heapSizeBefore();
            }

            if (message.heapSizeAfter() > heapAfterMax) {
                heapAfterMax = message.heapSizeAfter();
            }

            reclaimedTotalBytes += message.heapSizeBefore() - message.heapSizeAfter();
        }

        final var sortedData = data.stream()
                .map(e -> e.parsedMessage().timeSpent())
                .sorted()
                .toList();
        int medianIndex = (int) (sortedData.size() * 0.5);
        int p90Index = (int) (sortedData.size() * 0.9);
        int p95Index = (int) (sortedData.size() * 0.95);

        System.out.println("Total collections: " + totalCollections);
        System.out.println("Young collections: " + youngCollections);
        System.out.println("Mixed collections: " + mixedCollections);
        System.out.println("Full collections: " + fullCollections);
        System.out.println("Total pause ms: " + totalPauseMs);
        System.out.println("Average pause ms: " + (totalPauseMs / totalCollections));
        System.out.println("Max pause ms: " + maxPauseMs);
        System.out.println("P50 pause ms: " + sortedData.get(medianIndex));
        System.out.println("P90 pause ms: " + sortedData.get(p90Index));
        System.out.println("P95 pause ms: " + sortedData.get(p95Index));
        System.out.println("Heap before average: " + (heapBeforeAverage / totalCollections));
        System.out.println("Heap after average: " + (heapAfterAverage / totalCollections));
        System.out.println("Heap before max: " + heapBeforeMax);
        System.out.println("Heap after max: " + heapAfterMax);
        System.out.println("Reclaimed total bytes: " + reclaimedTotalBytes);
    }
}
