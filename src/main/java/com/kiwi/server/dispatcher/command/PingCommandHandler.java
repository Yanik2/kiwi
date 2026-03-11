package com.kiwi.server.dispatcher.command;

import com.kiwi.server.context.ConnectionContext;
import com.kiwi.server.request.model.TCPRequest;
import com.kiwi.server.response.model.PingResponse;
import com.kiwi.server.response.model.SerializableValue;

public class PingCommandHandler implements RequestCommandHandler {

    @Override
    public SerializableValue handle(TCPRequest request, ConnectionContext context) {
        return PingResponse.getInstance();
    }
}
