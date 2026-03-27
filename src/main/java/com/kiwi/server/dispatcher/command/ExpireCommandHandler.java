package com.kiwi.server.dispatcher.command;

import static com.kiwi.persistent.mutation.ErrorType.NOT_EXISTS;
import static com.kiwi.server.response.model.BinaryResponseValues.FAIL;
import static com.kiwi.server.response.model.BinaryResponseValues.SUCCESS;

import com.kiwi.persistent.StorageFacade;
import com.kiwi.persistent.model.Key;
import com.kiwi.persistent.model.Value;
import com.kiwi.persistent.model.expiration.HasTtlExpiration;
import com.kiwi.persistent.mutation.MutationDecision;
import com.kiwi.server.context.ConnectionContext;
import com.kiwi.server.dispatcher.OperationResult;
import com.kiwi.server.request.model.NumericRequest;
import com.kiwi.server.request.model.TCPRequest;

public class ExpireCommandHandler extends StorageCommandHandler {

    public ExpireCommandHandler(StorageFacade storageFacade) {
        super(storageFacade);
    }

    @Override
    public OperationResult handle(TCPRequest request, ConnectionContext context) {
        final var expireRequest = (NumericRequest) request;
        final var expirationTime = expireRequest.getValue() < 0 ? -1 : expireRequest.getValue();
        final var expiration = System.currentTimeMillis() + expirationTime;
        final var key = new Key(expireRequest.getKey());
        final var expiryPolicy = new HasTtlExpiration(expiration < 0 ? Long.MAX_VALUE : expiration);

        final var mutationResult = storageFacade.mutate(key, state -> {
            if (!state.exists()) {
                return new MutationDecision.Error(NOT_EXISTS);
            }

            if (expiryPolicy.hasTtl() && expiryPolicy.remainingTime(System.currentTimeMillis()) <= 0) {
                return new MutationDecision.Delete(true);
            }

            return new MutationDecision.Write(true, new Value(state.value().getValue(), expiryPolicy));
        });

        return new OperationResult(mutationResult.success() ? SUCCESS.getValue() : FAIL.getValue(),
                mutationResult.success());
    }

}
