package com.kiwi.server.dispatcher.command;

import com.kiwi.server.dto.TCPRequest;
import com.kiwi.server.response.SerializableValue;

public interface RequestCommandHandler {
    SerializableValue handle(TCPRequest request);
}
