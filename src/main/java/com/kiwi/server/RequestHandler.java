package com.kiwi.server;

import static com.kiwi.server.Method.EXT;
import static com.kiwi.server.util.ServerConstants.ERROR_MESSAGE;

import com.kiwi.server.dto.TCPRequest;
import com.kiwi.server.dto.TCPResponse;
import com.kiwi.exception.ProtocolException;
import com.kiwi.observability.RequestMetrics;
import java.io.InputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RequestHandler {
    private static final Logger log = Logger.getLogger(RequestHandler.class.getSimpleName());

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
            }
        } catch (SocketTimeoutException e) {
            log.log(Level.WARNING, "Socket timed out");
        } catch (Exception e) {
            log.log(Level.WARNING, "Unexpected exception during request processing: "
                + e.getMessage());
        }
        metrics.onClose();
    }
}
