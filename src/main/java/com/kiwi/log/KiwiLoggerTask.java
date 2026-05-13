package com.kiwi.log;

import java.util.UUID;
import java.util.logging.Level;

public record KiwiLoggerTask(
        String message,
        UUID connectionId,
        String reason,
        Level level,
        RequestContext requestContext
) {
}
