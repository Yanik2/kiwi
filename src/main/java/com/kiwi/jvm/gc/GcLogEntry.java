package com.kiwi.jvm.gc;

import java.time.LocalDateTime;
import java.util.List;

public record GcLogEntry(
        LocalDateTime timestamp,
        float uptime,
        String level,
        List<String> tags,
        int gcId,
        GcEntryType entryType,
        GcLogParsedMessage parsedMessage
) {

}
