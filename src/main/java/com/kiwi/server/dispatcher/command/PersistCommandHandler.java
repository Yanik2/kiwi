package com.kiwi.server.dispatcher.command;

import static com.kiwi.server.dispatcher.command.CommandConstants.BAD_RESPONSE;
import static com.kiwi.server.dispatcher.command.CommandConstants.SUCCESS_RESPONSE;

import com.kiwi.persistent.StorageFacade;
import com.kiwi.persistent.model.Key;
import com.kiwi.persistent.model.Value;
import com.kiwi.persistent.model.expiration.NoOpExpiration;
import com.kiwi.persistent.mutation.MutationDecision;
import com.kiwi.server.context.ConnectionContext;
import com.kiwi.server.request.model.ParsedRequest;
import com.kiwi.server.request.model.TCPRequest;
import com.kiwi.server.response.model.SerializableValue;

public class PersistCommandHandler extends StorageCommandHandler {
    public PersistCommandHandler(StorageFacade storageFacade) {
        super(storageFacade);
    }

    @Override
    public SerializableValue handle(TCPRequest request, ConnectionContext context) {
        final var parsedRequest = (ParsedRequest) request;
        final var result2 = storageFacade.mutate(new Key(parsedRequest.getKey()), state -> {
            if (!state.exists()) {
                return new MutationDecision.Error();
            }

            return new MutationDecision.Write(new Value(state.value().getValue(), NoOpExpiration.getInstance()));
        });
        return result2.success() ? SUCCESS_RESPONSE : BAD_RESPONSE;
    }
}
