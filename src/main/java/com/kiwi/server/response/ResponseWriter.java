package com.kiwi.server.response;

import static com.kiwi.server.response.dto.WriteResponseStatus.ERROR;
import static com.kiwi.server.response.dto.WriteResponseStatus.OK;
import static com.kiwi.server.util.ServerConstants.ERROR_PREFIX;
import static com.kiwi.server.util.ServerConstants.SEPARATOR;
import static com.kiwi.server.util.ServerConstants.SUCCESS_PREFIX;

import com.kiwi.log.KiwiLogger;
import com.kiwi.log.KiwiLoggerFactory;
import com.kiwi.log.RequestContext;
import com.kiwi.server.response.dto.WriteResponseResult;
import com.kiwi.server.response.model.TCPResponse;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class ResponseWriter {
    private static final KiwiLogger log = KiwiLoggerFactory.getLogger(ResponseWriter.class.getName());

    public WriteResponseResult writeResponse(OutputStream os, TCPResponse tcpResponse) {
        final var prefix = tcpResponse.isSuccess() ? SUCCESS_PREFIX : ERROR_PREFIX;
        final var baos = new ByteArrayOutputStream();

        try {
            writeToBaos(baos, prefix, tcpResponse);
            baos.writeTo(os);
            return new WriteResponseResult(baos.size(), OK);
        } catch (IOException ex) {
            log.error("Error during writing to output stream: ", ex.getMessage(), tcpResponse.connectionId(),
                    new RequestContext(tcpResponse.requestId(), tcpResponse.method()));
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
