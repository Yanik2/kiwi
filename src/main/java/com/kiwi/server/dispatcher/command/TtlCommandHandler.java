package com.kiwi.server.dispatcher.command;

import static com.kiwi.server.Method.TTL;

import com.kiwi.persistent.Storage;
import com.kiwi.persistent.model.Key;
import com.kiwi.server.context.ConnectionContext;
import com.kiwi.server.dto.ParsedRequest;
import com.kiwi.server.dto.TCPRequest;
import com.kiwi.server.response.SerializableValue;
import com.kiwi.server.response.TtlResponse;

public class TtlCommandHandler extends StorageCommandHandler {
    public TtlCommandHandler(Storage storage) {
        super(storage);
    }

    @Override
    public SerializableValue handle(TCPRequest request, ConnectionContext context) {
        final var parsedRequest = (ParsedRequest) request;
        long result = storage.getTtl(new Key(parsedRequest.getKey()));

        if (TTL.equals(parsedRequest.getMethod()) && result > 0) {
            result /= 1000;
        }
        return new TtlResponse(result);
    }
}
