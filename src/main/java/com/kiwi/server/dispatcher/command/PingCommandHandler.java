package com.kiwi.server.dispatcher.command;

import com.kiwi.server.dto.TCPRequest;
import com.kiwi.server.response.PingResponse;
import com.kiwi.server.response.SerializableValue;

public class PingCommandHandler implements RequestCommandHandler {

    @Override
    public SerializableValue handle(TCPRequest request) {
        return PingResponse.getInstance();
    }
}
