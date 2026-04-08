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

public class MultiSetCommandHandler extends StorageCommandHandler {
    public MultiSetCommandHandler(StorageFacade storageFacade) {
        super(storageFacade);
    }

    @Override
    public OperationResult handle(TCPRequest request, ConnectionContext context) {
        final var parsedRequest = (ParsedRequest) request;
        for (ParsedRequest.KeyValuePair kvp : parsedRequest.getKeyValues()) {
            storageFacade.write(new Key(kvp.getKey()), new Value(kvp.getValue(), NoOpExpiration.getInstance()));
        }

        return new OperationResult(EmptyResponse.getInstance(), true);
    }
}
