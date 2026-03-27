package com.kiwi.server.dispatcher.command;

import com.kiwi.persistent.StorageFacade;
import com.kiwi.persistent.model.Key;
import com.kiwi.persistent.model.Value;
import com.kiwi.persistent.model.expiration.NoOpExpiration;
import com.kiwi.server.context.ConnectionContext;
import com.kiwi.server.dispatcher.OperationResult;
import com.kiwi.server.request.model.ParsedRequest;
import com.kiwi.server.request.model.TCPRequest;
import com.kiwi.server.response.model.EmptyResponse;

public class SetCommandHandler extends StorageCommandHandler {

    public SetCommandHandler(StorageFacade storageFacade) {
        super(storageFacade);
    }

    @Override
    public OperationResult handle(TCPRequest request, ConnectionContext context) {
        final var parsedRequest = (ParsedRequest) request;
        final var key = new Key(parsedRequest.getKey());
        final var value = new Value(parsedRequest.getValue(), NoOpExpiration.getInstance());

        storageFacade.write(key, value);
        return new OperationResult(EmptyResponse.getInstance(), true);
    }


}
