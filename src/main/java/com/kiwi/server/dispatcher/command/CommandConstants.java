package com.kiwi.server.dispatcher.command;

import static com.kiwi.server.response.ResponseValueConstants.ONE_RESPONSE;
import static com.kiwi.server.response.ResponseValueConstants.ZERO_RESPONSE;

import com.kiwi.server.response.SerializableValue;

public final class CommandConstants {
    public static final SerializableValue BAD_RESPONSE = () -> ZERO_RESPONSE;
    public static final SerializableValue SUCCESS_RESPONSE = () -> ONE_RESPONSE;

    private CommandConstants() {}
}
