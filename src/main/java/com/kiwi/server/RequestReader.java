package com.kiwi.server;

import com.kiwi.exception.protocol.ProtocolException;
import com.kiwi.observability.RequestMetrics;
import com.kiwi.server.buffer.Cursor;
import com.kiwi.server.buffer.ReadBuffer;
import com.kiwi.server.dispatcher.command.RequestDispatcher;
import com.kiwi.server.dto.TCPResponse;
import com.kiwi.server.parsing.BinaryRequestParser;

import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.kiwi.server.util.ServerConstants.ERROR_MESSAGE;

public class RequestReader {
    private static final Logger log = Logger.getLogger(RequestReader.class.getName());

    private final BaseRequestValidator requestValidator;
    private final BinaryRequestParser requestParser;
    private final RequestMetrics requestMetrics;
    private final ResponseWriter responseWriter;
    private final RequestDispatcher requestDispatcher;

    public RequestReader(BaseRequestValidator requestValidator,
                         BinaryRequestParser binaryRequestParser,
                         RequestMetrics requestMetrics,
                         ResponseWriter responseWriter,
                         RequestDispatcher requestDispatcher) {
        this.requestValidator = requestValidator;
        this.requestParser = binaryRequestParser;
        this.requestMetrics = requestMetrics;
        this.responseWriter = responseWriter;
        this.requestDispatcher = requestDispatcher;
    }

    // TODO this method doesn't contain closing socket on EXT command
    // TODO closing connection will be implemented later in multithreading and response writer logic
    public void readRequest(Socket socket) {
        final var readBuffer = new ReadBuffer();
        final var cursor = new Cursor(readBuffer);

        try {
            final var is = socket.getInputStream();
            while (!socket.isClosed()) {
                final var bytesRead = readBuffer.fill(is);
                if (bytesRead == -1) {
                    break;
                }
                cursor.reset();
                final var parserResults = requestParser.parse(cursor);
                if (!parserResults.isEmpty()) {
                    parserResults.forEach(parserResult -> {
                        switch (parserResult.status()) {
                            case OK -> {
                                final var validatedRequest = requestValidator.validate(parserResult.value());
                                final var response = requestDispatcher.dispatch(validatedRequest);
                                final var writeResult = responseWriter.writeResponse(socket, response);
                                requestMetrics.onWrite(writeResult.writtenBytes());
                            }
                            case NEED_MORE_DATA -> {}
                            case ERROR -> throw parserResult.error();
                        }
                    });
                }

            }
        } catch (SocketTimeoutException ex) {
            log.log(Level.WARNING, "Socket timed out");
        } catch (ProtocolException ex) {
            onError(ex, socket);
        } catch (Exception ex) {
            log.severe("Unexpected exception during request processing: " + ex.getMessage());
        } finally {
            if (!socket.isClosed()) {
                try {
                    socket.close();
                } catch (Exception ex) {
                    // ignore
                }
            }
        }

        requestMetrics.onParse(readBuffer.getReadBytes());
        requestMetrics.onClose();
    }

    private void onError(ProtocolException ex, Socket socket) {
        log.severe("Unexpected error in protocol parsing: " + ex.getMessage());
        final var writeResult = responseWriter.writeResponse(socket, new TCPResponse(ERROR_MESSAGE, false));
        requestMetrics.onWrite(writeResult.writtenBytes());
        requestMetrics.onProtoError(ex.getProtocolErrorCode());
    }
}
