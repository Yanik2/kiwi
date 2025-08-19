package com.kiwi.server.util;

import java.nio.charset.StandardCharsets;

public final class ServerConstants {
    public static final String OK_MESSAGE = "OK";
    public static final String ERROR_MESSAGE = "ERROR";
    public static final String SUCCESS_PREFIX = "+";
    public static final String ERROR_PREFIX = "-";
    public static final byte[] SEPARATOR = "\r\n".getBytes(StandardCharsets.UTF_8);

    private ServerConstants() {}
}
