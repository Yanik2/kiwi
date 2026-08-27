package com.kiwi.jvm.gc.parser;

public final class MessageParsingConstants {
    public static final String ARROW_TOKEN = "->";
    public static final char BYTES_TOKEN = 'B';
    public static final char KILOBYTES_TOKEN = 'K';
    public static final char MEGABYTES_TOKEN = 'M';
    public static final char GIGABYTES_TOKEN = 'G';
    public static final String MILLISECONDS_TOKEN = "ms";

    public static final int BYTES_MULTIPLIER = 1024;

    private MessageParsingConstants() {
    }
}
