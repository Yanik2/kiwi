package com.kiwi.config.domain;

public record Config(
        int port,
        int backlog,
        int maxClients,
        int soTimeoutMillis,
        boolean metricsEnabled
) {
}
