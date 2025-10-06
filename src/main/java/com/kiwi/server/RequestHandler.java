package com.kiwi.server;

import static com.kiwi.server.Method.EXT;
import static com.kiwi.server.util.ServerConstants.ERROR_MESSAGE;

import com.kiwi.server.dto.TCPRequest;
import com.kiwi.server.dto.TCPResponse;
import com.kiwi.exception.protocol.ProtocolException;
import com.kiwi.observability.RequestMetrics;
import java.io.InputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RequestHandler {
    private static final Logger log = Logger.getLogger(RequestHandler.class.getSimpleName());
    //TODO will be moved to properties in phase 5
    private static final int MAX_CLIENTS = 1;

    private final RequestDispatcher requestDispatcher;
    private final RequestParser requestParser;
    private final ResponseWriter responseWriter;
    private final RequestMetrics metrics;

    public RequestHandler(RequestDispatcher requestDispatcher, RequestParser requestParser,
                          ResponseWriter responseWriter, RequestMetrics metrics) {
        this.requestDispatcher = requestDispatcher;
        this.requestParser = requestParser;
        this.responseWriter = responseWriter;
        this.metrics = metrics;
    }

    public void handle(Socket socket) {
        if (metrics.getCurrentClients() < MAX_CLIENTS) {
            acceptConnection(socket);
        } else {
            refuseConnection(socket);
        }
    }

    private void acceptConnection(Socket socket) {
        metrics.onAccept();
        try (socket) {
            try {
                final InputStream is = socket.getInputStream();
                while (true) {
                    final var isWrapper = new InputStreamWrapper(is);
                    final TCPRequest request = requestParser.parse(isWrapper);
                    metrics.onParse(isWrapper.getCounter());
                    final TCPResponse response = requestDispatcher.dispatch(request);
                    final var writeResult = responseWriter.writeResponse(socket, response);
                    metrics.onWrite(writeResult.writtenBytes());
                    if (EXT.equals(request.method())) {
                        break;
                    }
                }
            } catch (ProtocolException e) {
                log.log(Level.SEVERE, "Unexpected problem with protocol: " + e.getMessage());
                final var writeResult =
                    responseWriter.writeResponse(socket, new TCPResponse(ERROR_MESSAGE, false));
                metrics.onWrite(writeResult.writtenBytes());
                metrics.onProtoError(e.getProtocolErrorCode());
            }
        } catch (SocketTimeoutException e) {
            log.log(Level.WARNING, "Socket timed out");
        } catch (Exception e) {
            log.log(Level.WARNING, "Unexpected exception during request processing: "
                + e.getMessage());
        }
        metrics.onClose();
    }

    private void refuseConnection(Socket socket) {
        log.info("Maximum clients exceeded, refusing connection. Max clients: " + MAX_CLIENTS
            + ". Current clients: " + metrics.getCurrentClients());
        try {
            socket.setSoLinger(true, 0);
            metrics.onRefuse();
            socket.close();
        } catch (Exception e) {
            log.log(Level.WARNING, "Unexpected error during refusing connection");
        }
    }
}
