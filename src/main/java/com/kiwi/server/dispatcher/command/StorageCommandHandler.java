package com.kiwi.server.dispatcher.command;

import com.kiwi.persistent.storage.Storage;

public abstract class StorageCommandHandler implements RequestCommandHandler {
    protected final Storage storageFacade;

    protected StorageCommandHandler(Storage storageFacade) {
        this.storageFacade = storageFacade;
    }
}
