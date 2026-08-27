package com.kiwi.jvm.gc.parser;

import com.kiwi.exception.jvm.GcLogParsingException;
import com.kiwi.jvm.gc.GcEntryType;
import com.kiwi.jvm.gc.GcLogEntry;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import static com.kiwi.jvm.gc.GcEntryType.GC;
import static com.kiwi.jvm.gc.GcEntryType.OTHER;

public class GcLogParser {
    public static final char CLOSING_BRACKET = ')';
    private static final char OPENING_SQUARE_BRACKET = '[';
    private static final char CLOSING_SQUARE_BRACKET = ']';
    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    private static final int MAX_FILE_LENGTH = 1048576;
    private static final char DOT = '.';
    private static final char S_SYMBOL = 's';
    private static final char COMMA = ',';
    private static final String GC_MESSAGE_PREFIX = "GC";
    private static final String SAFEPOINT_MESSAGE_PREFIX = "Safepoint";

    private final GcLogMessageParserImpl baseMessageParser = new GcLogMessageParserImpl();

    public List<GcLogEntry> parse(String filename) {
        final var file = getFile(filename);
        try (final var reader = new BufferedReader(new FileReader(file))) {
            return reader.lines()
                    .map(getGcEntry())
                    .filter(Objects::nonNull)
                    .toList();
        } catch (Exception ex) {
            System.out.println("Problem with file: " + ex.getMessage());
            throw new GcLogParsingException("Problem with file", ex);
        }
    }

    private Function<String, GcLogEntry> getGcEntry() {
        return line -> {
            try {
                int currentIndex;
                var openingBracket = line.indexOf(OPENING_SQUARE_BRACKET);
                var closingBracket = line.indexOf(CLOSING_SQUARE_BRACKET, openingBracket);
                final var timestamp = LocalDateTime.parse(line.substring(openingBracket + 1, closingBracket),
                        DATE_TIME_FORMATTER);

                currentIndex = closingBracket + 1;
                openingBracket = line.indexOf(OPENING_SQUARE_BRACKET, currentIndex);
                closingBracket = line.indexOf(CLOSING_SQUARE_BRACKET, openingBracket);
                final var dotIndex = line.indexOf(DOT, openingBracket + 1);
                final var integerPart = line.substring(openingBracket + 1, dotIndex);
                final var sSymbolIndex = line.indexOf(S_SYMBOL, dotIndex, closingBracket);
                final var fractionalPart = line.substring(dotIndex + 1, sSymbolIndex);
                final float uptime = Integer.parseInt(integerPart) + (Integer.parseInt(fractionalPart) / 1000.0f);

                currentIndex = closingBracket + 1;
                openingBracket = line.indexOf(OPENING_SQUARE_BRACKET, currentIndex);
                closingBracket = line.indexOf(CLOSING_SQUARE_BRACKET, openingBracket);
                final var level = line.substring(openingBracket + 1, closingBracket);

                currentIndex = closingBracket + 1;
                openingBracket = line.indexOf(OPENING_SQUARE_BRACKET, currentIndex);
                closingBracket = line.indexOf(CLOSING_SQUARE_BRACKET, openingBracket);
                final var tags = getTags(line.substring(openingBracket + 1, closingBracket));
                currentIndex = closingBracket + 2;

                GcEntryType entryType;
                final var message = line.substring(currentIndex);
                int gcId = -1;
                if (message.startsWith(GC_MESSAGE_PREFIX)) {
                    entryType = GC;
                    gcId = Integer.parseInt(message.substring(3, message.indexOf(CLOSING_BRACKET, 3)));
                    currentIndex = line.indexOf(CLOSING_BRACKET, currentIndex) + 2;
                } else if (message.startsWith(SAFEPOINT_MESSAGE_PREFIX)) {
                    entryType = GcEntryType.SAFEPOINT;
                } else {
                    entryType = OTHER;
                }
                final var logMessage = line.substring(currentIndex);
                return new GcLogEntry(
                        timestamp,
                        uptime,
                        level,
                        tags,
                        GC.equals(entryType) ? gcId : -1,
                        entryType,
                        baseMessageParser.parse(logMessage)
                );
            } catch (Exception ex) {
                System.out.println("Error during parsing, row will be skipped");
                System.out.println("Exception: " + ex.getMessage());
                System.out.println("Line: " + line);
                return null;
            }
        };
    }

    private List<String> getTags(String rawValue) {
        if (rawValue.isEmpty()) {
            return List.of();
        }
        final var tags = new LinkedList<String>();
        int index = 0;

        int commaIndex;
        while ((commaIndex = rawValue.indexOf(COMMA, index)) != -1) {
            tags.add(rawValue.substring(index, commaIndex).trim());
            index = commaIndex + 1;
        }
        tags.add(rawValue.substring(index).trim());

        return tags;
    }

    private File getFile(String filename) {
        final var file = new File(filename);
        if (!file.exists()) {
            throw new GcLogParsingException("File [" + filename + "] doesn't exist");
        }
        if (file.length() > MAX_FILE_LENGTH) {
            throw new GcLogParsingException("File is too big. Size: " + file.length());
        }
        return file;
    }
}
