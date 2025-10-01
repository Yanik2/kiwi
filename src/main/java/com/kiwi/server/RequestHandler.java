package com.kiwi.server;

import static com.kiwi.server.Method.EXT;
import static com.kiwi.server.util.ServerConstants.ERROR_MESSAGE;

import com.kiwi.dto.TCPRequest;
import com.kiwi.dto.TCPResponse;
import com.kiwi.exception.ProtocolException;
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

    public RequestHandler(RequestDispatcher requestDispatcher, RequestParser requestParser,
                          ResponseWriter responseWriter) {
        this.requestDispatcher = requestDispatcher;
        this.requestParser = requestParser;
        this.responseWriter = responseWriter;
    }

    public void handle(Socket socket) {
        try (socket) {
            try {
                final InputStream is = socket.getInputStream();
                while (true) {
                    final TCPRequest request = requestParser.parse(is);
                    final TCPResponse response = requestDispatcher.dispatch(request);
                    responseWriter.writeResponse(socket, response);
                    if (EXT.equals(request.method())) {
                        break;
                    }
                }
            } catch (ProtocolException e) {
                log.log(Level.SEVERE, "Unexpected problem with protocol: " + e.getMessage());
                responseWriter.writeResponse(socket, new TCPResponse(ERROR_MESSAGE, false));
            }
        } catch (SocketTimeoutException e) {
            log.log(Level.WARNING, "Socket timed out");
        } catch (Exception e) {
            log.log(Level.WARNING, "Unexpected exception during request processing: "
                + e.getMessage());
        }
    }
}
