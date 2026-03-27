package com.kiwi.server.dispatcher.command;

import com.kiwi.server.context.ConnectionContext;
import com.kiwi.server.dispatcher.OperationResult;
import com.kiwi.server.request.model.TCPRequest;
import com.kiwi.server.response.model.PingResponse;

public class PingCommandHandler implements RequestCommandHandler {

    @Override
    public OperationResult handle(TCPRequest request, ConnectionContext context) {
        return new OperationResult(PingResponse.getInstance(), true);
    }
}
