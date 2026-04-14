package com.kiwi.server.dispatcher.command;

import static com.kiwi.server.request.Method.TTL;

import com.kiwi.persistent.storage.Storage;
import com.kiwi.persistent.model.Key;
import com.kiwi.server.context.ConnectionContext;
import com.kiwi.server.dispatcher.OperationResult;
import com.kiwi.server.request.model.ParsedRequest;
import com.kiwi.server.request.model.TCPRequest;
import com.kiwi.server.response.model.TtlResponse;

public class TtlCommandHandler extends StorageCommandHandler {
    private static final long TTL_RESPONSE_NOT_FOUND = -2L;
    private static final long TTL_RESPONSE_NO_TTL = -1L;

    public TtlCommandHandler(Storage storageFacade) {
        super(storageFacade);
    }

    @Override
    public OperationResult handle(TCPRequest request, ConnectionContext context) {
        final var parsedRequest = (ParsedRequest) request;
        final var value = storageFacade.read(new Key(parsedRequest.getKey()));

        if (value.isEmpty()) {
            return new OperationResult(new TtlResponse(TTL_RESPONSE_NOT_FOUND), true);
        }
        final var expiryPolicy = value.get().getExpiryPolicy();

        var ttl = expiryPolicy.hasTtl()
                ? expiryPolicy.remainingTime(System.currentTimeMillis())
                : TTL_RESPONSE_NO_TTL;

        if (TTL.equals(parsedRequest.getMethod()) && ttl > 0) {
            ttl /= 1000;
        }
        return new OperationResult(new TtlResponse(ttl), true);
    }
}
