package com.kiwi.server.dispatcher.command;

import com.kiwi.persistent.Storage;
import com.kiwi.server.response.SerializableValue;

public class PexpireCommandHandler extends StorageCommandHandler {
    public PexpireCommandHandler(Storage storage) {
        super(storage);
    }

    @Override
    public SerializableValue handle() {
        return null;
    }


}
