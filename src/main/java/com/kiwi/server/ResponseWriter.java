package com.kiwi.server;

import static com.kiwi.server.util.ServerConstants.ERROR_PREFIX;
import static com.kiwi.server.util.ServerConstants.SEPARATOR;
import static com.kiwi.server.util.ServerConstants.SUCCESS_PREFIX;

import com.kiwi.server.dto.TCPResponse;
import com.kiwi.server.dto.WriteResponseResult;
import java.io.ByteArrayOutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

public class ResponseWriter {
    private static final Logger log = Logger.getLogger(ResponseWriter.class.getSimpleName());

    public WriteResponseResult writeResponse(Socket socket, TCPResponse tcpResponse) {
        final var prefix = tcpResponse.isSuccess() ? SUCCESS_PREFIX : ERROR_PREFIX;
        final var baos = new ByteArrayOutputStream();
        writeToBaos(baos, prefix, tcpResponse);

        writeToOs(socket, baos);
        return new WriteResponseResult(baos.size());
    }

    private void writeToBaos(ByteArrayOutputStream baos, byte prefix, TCPResponse tcpResponse) {
        try {
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
        } catch (Exception ex) {
            log.severe("Unexpected error during writing response to output stream: "
                + ex.getMessage());
        }
    }

    private void writeToOs(Socket socket, ByteArrayOutputStream baos) {
        try {
            final var os = socket.getOutputStream();
            baos.writeTo(os);
        } catch (Exception ex) {
            log.severe("Unexpected exception during writing response to output stream: "
                + ex.getMessage());
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
