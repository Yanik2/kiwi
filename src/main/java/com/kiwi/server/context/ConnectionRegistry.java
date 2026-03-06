package com.kiwi.server.context;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ConnectionRegistry {
    private final ConcurrentMap<UUID, ConnectionContext> registry = new ConcurrentHashMap<>();

    public void register(ConnectionContext context) {
        registry.put(context.connectionId(), context);
    }

    public void unregister(ConnectionContext context) {
        registry.remove(context.connectionId());
    }

    public void shutdown() {
        registry.values().forEach(ConnectionContext::close);
    }
}
