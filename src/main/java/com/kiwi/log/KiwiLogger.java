package com.kiwi.log;

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
        this.log(message, reason, null, INFO, null);
    }

    public void info(String message, String reason, RequestContext requestContext) {
        this.log(message, reason, null, INFO, requestContext);
    }

    public void warn(String message) {
        this.warn(message, null);
    }

    public void warn(String message, String reason) {
        this.log(message, reason, null, WARNING, null);
    }

    public void warn(String message, String reason, RequestContext requestContext) {
        this.log(message, reason, null, WARNING, requestContext);
    }

    public void warn(String message, String reason, UUID connectionId) {
        this.log(message, reason, connectionId, WARNING, null);
    }

    public void error(String message, String reason, UUID connectionId) {
        this.log(message, reason, connectionId, SEVERE, null);
    }

    public void error(String message) {
        this.log(message, null, null, SEVERE, null);
    }

    public void error(String message, String reason) {
        this.log(message, reason, null, SEVERE, null);
    }

    public void error(String message, String reason, UUID connectionId, RequestContext requestContext) {
        this.log(message, reason, connectionId, SEVERE, requestContext);
    }

    public void error(String message, UUID connectionId, RequestContext requestContext) {
        this.log(message, null, connectionId, SEVERE, requestContext);
    }

    private void log(String message, String reason, UUID connectionId, Level level, RequestContext requestContext) {
        this.submitTask(new KiwiLoggerTask(message, connectionId, reason, level, requestContext));
    }

    private void submitTask(KiwiLoggerTask task) {
        executor.execute(() -> {
            final var sb = new StringBuilder();
            sb.append("source=");
            sb.append(name);
            sb.append(" ");

            if (task.requestContext() != null) {
                sb.append("req_id=");
                sb.append(task.requestContext().requestId());
                sb.append(" ");
                if (task.requestContext().method() != null) {
                    sb.append("method=");
                    sb.append(task.requestContext().method());
                    sb.append(" ");
                }
            }

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
