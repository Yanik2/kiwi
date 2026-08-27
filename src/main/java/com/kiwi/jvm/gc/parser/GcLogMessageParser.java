package com.kiwi.jvm.gc.parser;

import com.kiwi.jvm.gc.GcCycleType;
import com.kiwi.jvm.gc.GcLogParsedMessage;

import static com.kiwi.jvm.gc.GcCycleType.UNKNOWN;
import static com.kiwi.jvm.gc.parser.MessageParsingConstants.ARROW_TOKEN;
import static com.kiwi.jvm.gc.parser.MessageParsingConstants.BYTES_MULTIPLIER;
import static com.kiwi.jvm.gc.parser.MessageParsingConstants.GIGABYTES_TOKEN;
import static com.kiwi.jvm.gc.parser.MessageParsingConstants.KILOBYTES_TOKEN;
import static com.kiwi.jvm.gc.parser.MessageParsingConstants.MEGABYTES_TOKEN;
import static com.kiwi.jvm.gc.parser.MessageParsingConstants.MILLISECONDS_TOKEN;

public class GcLogMessageParser {
    public GcLogParsedMessage parse(String message) {
        final var cycleType = GcCycleType.getByMessage(message);

        if (UNKNOWN.equals(cycleType)) {
            return new GcLogParsedMessage(UNKNOWN, -1, -1, -1, -1.0f, message);
        }

        final int arrowIndex = message.indexOf(ARROW_TOKEN);
        if (arrowIndex == -1) {
            // this is start gc event
            return new GcLogParsedMessage(cycleType, -1, -1, -1, -1.0f, message);
        }

        var measureTokenIndex = arrowIndex - 1;
        final char beforeMeasureToken = message.charAt(measureTokenIndex);
        final int beforeSizeIndex;
        while (message.charAt(--measureTokenIndex) != ' ');
        beforeSizeIndex = measureTokenIndex + 1;
        final int afterSizeIndex = arrowIndex + 2;
        int afterMeasureTokenIndex = afterSizeIndex;
        char afterMeasureToken;
        while ((afterMeasureToken = message.charAt(afterMeasureTokenIndex++)) >= '0' && afterMeasureToken <= '9');
        final var beforeSize = getBytesByMeasure(
                Integer.parseInt(message.substring(beforeSizeIndex, arrowIndex - 1)), beforeMeasureToken
        );
        final var afterSize = getBytesByMeasure(
                Integer.parseInt(message.substring(afterSizeIndex, --afterMeasureTokenIndex)), afterMeasureToken
        );

        final var totalSizeIndex = afterMeasureTokenIndex + 2;
        int totalMeasureTokenIndex = totalSizeIndex;
        char totalMeasureToken;
        while ((totalMeasureToken = message.charAt(totalMeasureTokenIndex++)) >= '0' && totalMeasureToken <= '9');
        final var totalSize = getBytesByMeasure(
                Integer.parseInt(message.substring(totalSizeIndex, --totalMeasureTokenIndex)), totalMeasureToken
        );

        final var millisecondsTokenIndex = message.indexOf(MILLISECONDS_TOKEN, totalMeasureTokenIndex);
        int startTimeIndex = millisecondsTokenIndex;
        while (message.charAt(startTimeIndex--) != ' ');
        final float timeMillis = Float.parseFloat(message.substring(++startTimeIndex, millisecondsTokenIndex));

        return new GcLogParsedMessage(cycleType, beforeSize, afterSize, totalSize, timeMillis, message);
    }

    private long getBytesByMeasure(int size, char measure) {
        return switch (measure) {
            case KILOBYTES_TOKEN -> size * BYTES_MULTIPLIER;
            case MEGABYTES_TOKEN -> size * BYTES_MULTIPLIER * BYTES_MULTIPLIER;
            case GIGABYTES_TOKEN -> size * BYTES_MULTIPLIER * BYTES_MULTIPLIER * BYTES_MULTIPLIER;
            default -> size;
        };
    }
}
