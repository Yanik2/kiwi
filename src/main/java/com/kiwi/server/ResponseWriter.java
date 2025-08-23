package com.kiwi.server;

import static com.kiwi.server.util.ServerConstants.ERROR_PREFIX;
import static com.kiwi.server.util.ServerConstants.OK_MESSAGE;
import static com.kiwi.server.util.ServerConstants.SEPARATOR;
import static com.kiwi.server.util.ServerConstants.SUCCESS_PREFIX;

import com.kiwi.dto.TCPResponse;
import java.io.ByteArrayOutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

public class ResponseWriter {
    private static final Logger log = Logger.getLogger(ResponseWriter.class.getSimpleName());

    public void writeResponse(Socket socket, TCPResponse tcpResponse) {
        final var prefix = OK_MESSAGE.equals(tcpResponse.message()) ? SUCCESS_PREFIX : ERROR_PREFIX;
        final var baos = new ByteArrayOutputStream();
        writeToBaos(baos, prefix, tcpResponse);

        writeToOs(socket, baos);
    }

    private void writeToBaos(ByteArrayOutputStream baos, String prefix, TCPResponse tcpResponse) {
        try {
            baos.write(prefix.getBytes(StandardCharsets.UTF_8));
            baos.write(tcpResponse.message().getBytes(StandardCharsets.UTF_8));
            baos.write(SEPARATOR);
            final int payloadLength = tcpResponse.value() != null
                ? tcpResponse.value().getValue().length
                : 0;

            baos.write(payloadLength + 48);
            baos.write(SEPARATOR);
            baos.write(tcpResponse.value().getValue());
            baos.write(SEPARATOR);
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
}
