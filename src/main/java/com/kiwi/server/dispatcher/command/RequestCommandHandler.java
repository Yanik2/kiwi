package com.kiwi.server.dispatcher.command;

import com.kiwi.server.context.ConnectionContext;
import com.kiwi.server.request.model.TCPRequest;
import com.kiwi.server.response.model.SerializableValue;

public interface RequestCommandHandler {
    SerializableValue handle(TCPRequest request, ConnectionContext context);
}
