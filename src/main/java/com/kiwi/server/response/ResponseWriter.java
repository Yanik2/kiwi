package com.kiwi.server.response;

import static com.kiwi.server.response.dto.WriteResponseStatus.ERROR;
import static com.kiwi.server.response.dto.WriteResponseStatus.OK;
import static com.kiwi.server.util.ServerConstants.ERROR_PREFIX;
import static com.kiwi.server.util.ServerConstants.SEPARATOR;
import static com.kiwi.server.util.ServerConstants.SUCCESS_PREFIX;

import com.kiwi.server.response.dto.WriteResponseResult;
import com.kiwi.server.response.model.TCPResponse;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

public class ResponseWriter {
    private static final Logger log = Logger.getLogger(ResponseWriter.class.getSimpleName());

    public WriteResponseResult writeResponse(OutputStream os, TCPResponse tcpResponse) {
        final var prefix = tcpResponse.isSuccess() ? SUCCESS_PREFIX : ERROR_PREFIX;
        final var baos = new ByteArrayOutputStream();

        try {
            writeToBaos(baos, prefix, tcpResponse);
            baos.writeTo(os);
            return new WriteResponseResult(baos.size(), OK);
        } catch (IOException ex) {
            log.severe("Error during writing to output stream: " + ex.getMessage());
            return new WriteResponseResult(0, ERROR);
        }
    }

    private void writeToBaos(ByteArrayOutputStream baos, byte prefix, TCPResponse tcpResponse) throws IOException {
        baos.write(prefix);
        baos.write(tcpResponse.message().getBytes(StandardCharsets.UTF_8));
        baos.write(SEPARATOR);

        final byte[] responsePayload = tcpResponse.responsePayload().serialize();

        writePayloadLength(responsePayload.length, baos);
        baos.write(SEPARATOR);
        if (responsePayload.length > 0) {
            baos.write(responsePayload);
            baos.write(SEPARATOR);
        }

    }

    private void writePayloadLength(int len, ByteArrayOutputStream baos) {
        if (len == 0) {
            baos.write(48);
            return;
        }

        int divider = 10_000_000;

        while (divider > len) {
            divider /= 10;
        }

        while (divider != 0) {
            final var order = len / divider;
            baos.write(order + 48);
            len -= (divider * order);
            divider /= 10;
        }
    }
}
