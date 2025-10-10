package com.kiwi.server.dispatcher.command;

import com.kiwi.persistent.Storage;
import com.kiwi.persistent.dto.StorageRequest;
import com.kiwi.persistent.model.Key;
import com.kiwi.persistent.model.Value;
import com.kiwi.persistent.model.expiration.NoOpExpiration;
import com.kiwi.server.dto.ParsedRequest;
import com.kiwi.server.dto.TCPRequest;
import com.kiwi.server.response.EmptyResponse;
import com.kiwi.server.response.SerializableValue;

public class SetCommandHandler extends StorageCommandHandler {

    public SetCommandHandler(Storage storage) {
        super(storage);
    }

    @Override
    public SerializableValue handle(TCPRequest request) {
        final var parsedRequest = (ParsedRequest) request;
        final var key = new Key(parsedRequest.getKey());
        final var value = new Value(parsedRequest.getValue(), NoOpExpiration.getInstance());

        storage.save(new StorageRequest(key, value));

        return EmptyResponse.getInstance();
    }


}
