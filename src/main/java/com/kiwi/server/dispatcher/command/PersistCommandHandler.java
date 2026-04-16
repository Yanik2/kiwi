package com.kiwi.server.dispatcher.command;

import static com.kiwi.persistent.mutation.ErrorType.NOT_EXISTS;
import static com.kiwi.server.response.model.BinaryResponseValues.FAIL;
import static com.kiwi.server.response.model.BinaryResponseValues.SUCCESS;

import com.kiwi.observability.metrics.OperationErrorMetrics;
import com.kiwi.persistent.storage.Storage;
import com.kiwi.persistent.model.Key;
import com.kiwi.persistent.model.Value;
import com.kiwi.persistent.model.expiration.NoOpExpiration;
import com.kiwi.persistent.mutation.MutationDecision;
import com.kiwi.server.context.ConnectionContext;
import com.kiwi.server.dispatcher.OperationResult;
import com.kiwi.server.request.model.ParsedRequest;
import com.kiwi.server.request.model.TCPRequest;

public class PersistCommandHandler extends StorageCommandHandler {
    private final OperationErrorMetrics operationErrorMetrics;

    public PersistCommandHandler(Storage storageFacade, OperationErrorMetrics operationErrorMetrics) {
        super(storageFacade);
        this.operationErrorMetrics = operationErrorMetrics;
    }

    @Override
    public OperationResult handle(TCPRequest request, ConnectionContext context) {
        final var parsedRequest = (ParsedRequest) request;
        final var mutationResult = storageFacade.mutate(new Key(parsedRequest.getKey()), state -> {
            if (!state.exists()) {
                operationErrorMetrics.onError(NOT_EXISTS);
                return new MutationDecision.Error(NOT_EXISTS);
            }

            return new MutationDecision.Write(true, new Value(state.value().getValue(), NoOpExpiration.getInstance()));
        });
        return new OperationResult(mutationResult.success() ? SUCCESS.getValue() : FAIL.getValue(),
                mutationResult.success());
    }
}
