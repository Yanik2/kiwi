package com.kiwi.server.context;

import java.util.LinkedList;
import java.util.List;

public class ConnectionRegistry {
    private final List<ConnectionContext> registry = new LinkedList<>();

    public void register(ConnectionContext context) {
        registry.add(context);
    }

    public void unRegister() {
        //TODO will be implemented when requirements will be clear
    }
}
