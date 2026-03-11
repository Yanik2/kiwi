package com.kiwi.server.dispatcher.command;

import com.kiwi.server.context.ConnectionContext;
import com.kiwi.server.request.model.TCPRequest;
import com.kiwi.server.response.model.EmptyResponse;
import com.kiwi.server.response.model.SerializableValue;

public class ExitCommandHandler implements RequestCommandHandler {

    @Override
    public SerializableValue handle(TCPRequest request, ConnectionContext context) {
        context.close();
        return EmptyResponse.getInstance();
    }
}
