package com.kiwi.log;

import com.kiwi.server.context.ConnectionContext;

import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.util.logging.Level.INFO;
import static java.util.logging.Level.SEVERE;
import static java.util.logging.Level.WARNING;

public class KiwiLogger {
    private final Executor executor;
    private final Logger logger;
    private final String name;

    public KiwiLogger(Executor executor, String name, Logger logger) {
        this.executor = executor;
        this.logger = logger;
        this.name = name;
    }

    public void info(String message) {
        this.info(message, null);
    }

    public void info(String message, String reason) {
        this.log(message, reason, null, INFO);
    }

    public void warn(String message) {
        this.warn(message, null);
    }

    public void warn(String message, String reason) {
        this.log(message, reason, null, WARNING);
    }

    public void warn(String message, String reason, UUID connectionId) {
        this.log(message, reason, connectionId, WARNING);
    }

    public void error(String message, ConnectionContext context) {
        this.log(message, null, context.connectionId(), SEVERE);
    }

    public void error(String message) {
        this.log(message, null, null, SEVERE);
    }

    public void error(String message, String reason) {
        this.log(message, reason, null, SEVERE);
    }

    public void error(String message, String reason, UUID connectionId) {
        this.log(message, reason, connectionId, SEVERE);
    }

    public void error(String message, UUID connectionId) {
        this.log(message, null, connectionId, SEVERE);
    }

    private void log(String message, String reason, UUID connectionId, Level level) {
        this.submitTask(new KiwiLoggerTask(message, connectionId, reason, level));
    }

    private void submitTask(KiwiLoggerTask task) {
        executor.execute(() -> {
            final var sb = new StringBuilder();
            sb.append("source=");
            sb.append(name);
            sb.append(" ");

            if (task.connectionId() != null) {
                sb.append("conn_id=");
                sb.append(task.connectionId());
                sb.append(" ");
            }

            if (task.message() != null) {
                sb.append("message=");
                sb.append(task.message());
                sb.append(" ");
            }

            if (task.reason() != null) {
                sb.append("reason=");
                sb.append(task.reason());
                sb.append(" ");
            }

            final var logMessage = sb.toString();

            logger.log(task.level(), logMessage);
        });
    }
}
