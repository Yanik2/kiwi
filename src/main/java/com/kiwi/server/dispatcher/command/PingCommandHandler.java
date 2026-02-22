package com.kiwi.server.dispatcher.command;

import com.kiwi.server.context.ConnectionContext;
import com.kiwi.server.dto.TCPRequest;
import com.kiwi.server.response.PingResponse;
import com.kiwi.server.response.SerializableValue;

public class PingCommandHandler implements RequestCommandHandler {

    @Override
    public SerializableValue handle(TCPRequest request, ConnectionContext context) {
        return PingResponse.getInstance();
    }
}
