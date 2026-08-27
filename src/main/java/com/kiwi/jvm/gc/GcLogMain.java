package com.kiwi.jvm.gc;

import com.kiwi.jvm.gc.parser.GcLogParser;
import com.kiwi.jvm.gc.report.GcReportService;

public class GcLogMain {

    public static void main(String[] args) {
        final var logParser = new GcLogParser();
        final var reportService = new GcReportService(logParser);
        reportService.displayReport(args[0]);
        System.out.println();
    }
}
