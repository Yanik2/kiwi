package com.kiwi.server.dispatcher.command;

import com.kiwi.persistent.Storage;

public abstract class StorageCommandHandler implements RequestCommandHandler {
    protected final Storage storage;

    protected StorageCommandHandler(Storage storage) {
        this.storage = storage;
    }
}
