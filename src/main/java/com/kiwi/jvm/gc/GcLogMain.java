package com.kiwi.jvm.gc;

import com.kiwi.jvm.gc.parser.GcLogParser;
import com.kiwi.jvm.gc.report.GcReportService;

public class GcLogMain {

    public void test(String value) {
        System.out.println(value);
    }

    public static void main(String[] args) {
        final var logParser = new GcLogParser();
        final var reportService = new GcReportService(logParser);
        reportService.displayReport("./logs/gc.log");
        System.out.println();
    }
}
