package com.kiwi.server.dispatcher.command;

import com.kiwi.persistent.Storage;
import com.kiwi.persistent.dto.StorageRequest;
import com.kiwi.persistent.model.Key;
import com.kiwi.server.context.ConnectionContext;
import com.kiwi.server.dto.ParsedRequest;
import com.kiwi.server.dto.TCPRequest;
import com.kiwi.server.response.DataResponse;
import com.kiwi.server.response.SerializableValue;

public class GetCommandHandler extends StorageCommandHandler {
    public GetCommandHandler(Storage storage) {
        super(storage);
    }

    @Override
    public SerializableValue handle(TCPRequest request, ConnectionContext context) {
        final var parsedRequest = (ParsedRequest) request;
        final var value = storage.getData(new StorageRequest(new Key(parsedRequest.getKey())));
        return new DataResponse(value);
    }
}
