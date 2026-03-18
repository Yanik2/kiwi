package com.kiwi.server.dispatcher.command;

import com.kiwi.persistent.StorageFacade;
import com.kiwi.persistent.model.Key;
import com.kiwi.persistent.model.Value;
import com.kiwi.server.context.ConnectionContext;
import com.kiwi.server.request.model.ParsedRequest;
import com.kiwi.server.request.model.TCPRequest;
import com.kiwi.server.response.model.DataResponse;
import com.kiwi.server.response.model.SerializableValue;

public class GetCommandHandler extends StorageCommandHandler {
    public GetCommandHandler(StorageFacade storageFacade) {
        super(storageFacade);
    }

    @Override
    public SerializableValue handle(TCPRequest request, ConnectionContext context) {
        final var parsedRequest = (ParsedRequest) request;
        final var value = storageFacade.read(new Key(parsedRequest.getKey()));
        return new DataResponse(value.orElseGet(() -> new Value(new byte[0])));
    }
}
