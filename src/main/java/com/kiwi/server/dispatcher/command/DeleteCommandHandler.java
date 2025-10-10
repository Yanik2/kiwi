package com.kiwi.server.dispatcher.command;

import com.kiwi.persistent.Storage;
import com.kiwi.persistent.dto.StorageRequest;
import com.kiwi.persistent.model.Key;
import com.kiwi.server.dto.ParsedRequest;
import com.kiwi.server.dto.TCPRequest;
import com.kiwi.server.response.EmptyResponse;
import com.kiwi.server.response.SerializableValue;

public class DeleteCommandHandler extends StorageCommandHandler {
    public DeleteCommandHandler(Storage storage) {
        super(storage);
    }

    @Override
    public SerializableValue handle(TCPRequest request) {
        final var parsedRequest = (ParsedRequest) request;
        storage.delete(new StorageRequest(new Key(parsedRequest.getKey())));

        return EmptyResponse.getInstance();
    }


}
