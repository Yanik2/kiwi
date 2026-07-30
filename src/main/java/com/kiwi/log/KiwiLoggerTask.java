package com.kiwi.log;

import java.util.logging.Level;

public record KiwiLoggerTask(
        String message,
        long connectionId,
        boolean hasConnectionId,
        String reason,
        Level level,
        RequestContext requestContext
) {
}
