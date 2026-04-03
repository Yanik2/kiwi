package com.kiwi.server.dispatcher.command;

import com.kiwi.server.context.ConnectionContext;
import com.kiwi.server.dispatcher.OperationResult;
import com.kiwi.server.request.model.TCPRequest;

public interface RequestCommandHandler {
    OperationResult handle(TCPRequest request, ConnectionContext context);
}
