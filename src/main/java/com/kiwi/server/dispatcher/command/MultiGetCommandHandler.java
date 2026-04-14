package com.kiwi.server.dispatcher.command;

import com.kiwi.persistent.storage.Storage;
import com.kiwi.persistent.model.Key;
import com.kiwi.persistent.model.Value;
import com.kiwi.server.context.ConnectionContext;
import com.kiwi.server.dispatcher.OperationResult;
import com.kiwi.server.request.model.ParsedRequest;
import com.kiwi.server.request.model.TCPRequest;
import com.kiwi.server.response.model.MultiGetResponse;

import java.util.LinkedList;
import java.util.Optional;

public class MultiGetCommandHandler extends StorageCommandHandler {
    public MultiGetCommandHandler(Storage storageFacade) {
        super(storageFacade);
    }

    @Override
    public OperationResult handle(TCPRequest request, ConnectionContext context) {
        final var parsedRequest = (ParsedRequest) request;
        final var keys = parsedRequest.getKeys();
        final var result = new LinkedList<Optional<Value>>();
        for (byte[] key : keys) {
            result.add(storageFacade.read(new Key(key)));
        }
        return new OperationResult(new MultiGetResponse(result), true);
    }
}
