package com.kiwi.server.dispatcher.command;

import com.kiwi.persistent.StorageFacade;
import com.kiwi.persistent.model.Key;
import com.kiwi.persistent.model.Value;
import com.kiwi.persistent.model.expiration.NoOpExpiration;
import com.kiwi.persistent.mutation.MutationDecision;
import com.kiwi.server.context.ConnectionContext;
import com.kiwi.server.dispatcher.OperationResult;
import com.kiwi.server.request.model.ParsedRequest;
import com.kiwi.server.request.model.TCPRequest;

import static com.kiwi.server.response.model.BinaryResponseValues.FAIL;
import static com.kiwi.server.response.model.BinaryResponseValues.SUCCESS;

public class SetNxCommandHandler extends StorageCommandHandler {
    public SetNxCommandHandler(StorageFacade storageFacade) {
        super(storageFacade);
    }

    @Override
    public OperationResult handle(TCPRequest request, ConnectionContext context) {
        final var parsedRequest = (ParsedRequest) request;

        final var mutationResult = storageFacade.mutate(new Key(parsedRequest.getKey()), state -> {
            if (state.exists()) {
                return new MutationDecision.NoOp(false);
            } else {
                return new MutationDecision.Write(true, new Value(parsedRequest.getValue(),
                        NoOpExpiration.getInstance()));
            }
        });

        return new OperationResult(mutationResult.success() ? SUCCESS.getValue() : FAIL.getValue(),
                mutationResult.success());
    }
}
