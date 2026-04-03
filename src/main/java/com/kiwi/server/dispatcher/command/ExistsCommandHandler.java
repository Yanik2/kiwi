package com.kiwi.server.dispatcher.command;

import com.kiwi.persistent.StorageFacade;
import com.kiwi.persistent.model.Key;
import com.kiwi.server.context.ConnectionContext;
import com.kiwi.server.dispatcher.OperationResult;
import com.kiwi.server.request.model.ParsedRequest;
import com.kiwi.server.request.model.TCPRequest;

import static com.kiwi.server.response.model.BinaryResponseValues.FAIL;
import static com.kiwi.server.response.model.BinaryResponseValues.SUCCESS;

public class ExistsCommandHandler extends StorageCommandHandler {
    public ExistsCommandHandler(StorageFacade storageFacade) {
        super(storageFacade);
    }

    @Override
    public OperationResult handle(TCPRequest request, ConnectionContext context) {
        final var parsedRequest = (ParsedRequest) request;
        final var value = storageFacade.read(new Key(parsedRequest.getKey()));

        return new OperationResult(value.isPresent() ? SUCCESS.getValue() : FAIL.getValue(), true);
    }
}
