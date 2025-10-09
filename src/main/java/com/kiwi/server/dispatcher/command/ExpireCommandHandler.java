package com.kiwi.server.dispatcher.command;

import static com.kiwi.server.response.ResponseValueConstants.ONE_RESPONSE;
import static com.kiwi.server.response.ResponseValueConstants.ZERO_RESPONSE;

import com.kiwi.persistent.Storage;
import com.kiwi.persistent.dto.StorageRequest;
import com.kiwi.persistent.model.Key;
import com.kiwi.persistent.model.expiration.HasTtlExpiration;
import com.kiwi.server.dto.ExpireRequest;
import com.kiwi.server.dto.TCPRequest;
import com.kiwi.server.response.SerializableValue;

public class ExpireCommandHandler extends StorageCommandHandler {
    private static final SerializableValue BAD_RESPONSE = () -> ZERO_RESPONSE;
    private static final SerializableValue SUCCESS_RESPONSE = () -> ONE_RESPONSE;

    public ExpireCommandHandler(Storage storage) {
        super(storage);
    }

    @Override
    public SerializableValue handle(TCPRequest request) {
        final var expireRequest = (ExpireRequest) request;

        final var result =
            storage.updateExpiration(new Key(expireRequest.getKey()),
                new HasTtlExpiration(System.currentTimeMillis() + expireRequest.getValue()));

        return result ? SUCCESS_RESPONSE : BAD_RESPONSE;
    }


}
