package com.kiwi.server.dispatcher.command;

import com.kiwi.server.dto.TCPRequest;
import com.kiwi.server.response.EmptyResponse;
import com.kiwi.server.response.SerializableValue;

public class ExitCommandHandler implements RequestCommandHandler {

    @Override
    public SerializableValue handle(TCPRequest request) {
        return EmptyResponse.getInstance();
    }
}
