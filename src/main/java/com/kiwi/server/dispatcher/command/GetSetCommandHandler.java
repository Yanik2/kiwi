package com.kiwi.server.dispatcher.command;

import com.kiwi.persistent.storage.Storage;
import com.kiwi.persistent.model.Key;
import com.kiwi.persistent.model.Value;
import com.kiwi.persistent.model.expiration.NoOpExpiration;
import com.kiwi.persistent.mutation.MutationDecision;
import com.kiwi.server.context.ConnectionContext;
import com.kiwi.server.dispatcher.OperationResult;
import com.kiwi.server.request.model.ParsedRequest;
import com.kiwi.server.request.model.TCPRequest;
import com.kiwi.server.response.model.DataResponse;
import com.kiwi.server.response.model.EmptyResponse;

public class GetSetCommandHandler extends StorageCommandHandler {
    public GetSetCommandHandler(Storage storageFacade) {
        super(storageFacade);
    }

    @Override
    public OperationResult handle(TCPRequest request, ConnectionContext context) {
        final var parsedRequest = (ParsedRequest) request;
        final var mutationResult = storageFacade.mutate(new Key(parsedRequest.getKey()), state -> {
            if (state.exists()) {
                return new MutationDecision.Write(true,
                        new Value(parsedRequest.getValue(), NoOpExpiration.getInstance()), state.value());
            } else {
                return new MutationDecision.Write(true,
                        new Value(parsedRequest.getValue(), NoOpExpiration.getInstance()), null);
            }
        });

        return new OperationResult(mutationResult.value().isPresent()
                ? new DataResponse(mutationResult.value().get())
                : EmptyResponse.getInstance(), true);
    }
}
