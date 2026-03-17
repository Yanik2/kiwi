package com.kiwi.server.dispatcher.command;

import com.kiwi.persistent.StorageFacade;

public abstract class StorageCommandHandler implements RequestCommandHandler {
    protected final StorageFacade storageFacade;

    protected StorageCommandHandler(StorageFacade storageFacade) {
        this.storageFacade = storageFacade;
    }
}
