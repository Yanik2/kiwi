package com.kiwi.server.dispatcher.command;

import static com.kiwi.server.dispatcher.command.CommandConstants.BAD_RESPONSE;
import static com.kiwi.server.dispatcher.command.CommandConstants.SUCCESS_RESPONSE;

import com.kiwi.persistent.Storage;
import com.kiwi.persistent.model.Key;
import com.kiwi.server.dto.ParsedRequest;
import com.kiwi.server.dto.TCPRequest;
import com.kiwi.server.response.SerializableValue;

public class PersistCommandHandler extends StorageCommandHandler {
    public PersistCommandHandler(Storage storage) {
        super(storage);
    }

    @Override
    public SerializableValue handle(TCPRequest request) {
        final var parsedRequest = (ParsedRequest) request;
        final var result = storage.persist(new Key(parsedRequest.getKey()));
        return result ? SUCCESS_RESPONSE : BAD_RESPONSE;
    }
}
