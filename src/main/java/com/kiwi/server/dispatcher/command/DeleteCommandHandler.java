package com.kiwi.server.dispatcher.command;

import com.kiwi.persistent.StorageFacade;
import com.kiwi.persistent.model.Key;
import com.kiwi.server.context.ConnectionContext;
import com.kiwi.server.dispatcher.OperationResult;
import com.kiwi.server.request.model.ParsedRequest;
import com.kiwi.server.request.model.TCPRequest;
import com.kiwi.server.response.model.EmptyResponse;

public class DeleteCommandHandler extends StorageCommandHandler {
    public DeleteCommandHandler(StorageFacade storageFacade) {
        super(storageFacade);
    }

    @Override
    public OperationResult handle(TCPRequest request, ConnectionContext context) {
        final var parsedRequest = (ParsedRequest) request;
        storageFacade.delete(new Key(parsedRequest.getKey()));

        return new OperationResult(EmptyResponse.getInstance(), true);
    }


}
