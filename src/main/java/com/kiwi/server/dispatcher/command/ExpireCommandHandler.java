package com.kiwi.server.dispatcher.command;

import static com.kiwi.server.Method.EXPIRE;
import static com.kiwi.server.dispatcher.command.CommandConstants.BAD_RESPONSE;
import static com.kiwi.server.dispatcher.command.CommandConstants.SUCCESS_RESPONSE;

import com.kiwi.persistent.Storage;
import com.kiwi.persistent.model.Key;
import com.kiwi.persistent.model.expiration.HasTtlExpiration;
import com.kiwi.server.dto.ExpireRequest;
import com.kiwi.server.dto.TCPRequest;
import com.kiwi.server.response.SerializableValue;

public class ExpireCommandHandler extends StorageCommandHandler {

    public ExpireCommandHandler(Storage storage) {
        super(storage);
    }

    @Override
    public SerializableValue handle(TCPRequest request) {
        final var expireRequest = (ExpireRequest) request;
        final var expirationTime = expireRequest.getValue() < 0 ? -1 : expireRequest.getValue();
        final var expiration = System.currentTimeMillis() + expirationTime;


        final var result = storage.updateExpiration(new Key(expireRequest.getKey()),
                new HasTtlExpiration(expiration < 0 ? Long.MAX_VALUE : expiration));

        return result ? SUCCESS_RESPONSE : BAD_RESPONSE;
    }

}
