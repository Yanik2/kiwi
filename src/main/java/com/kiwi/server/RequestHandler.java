package com.kiwi.server;

import static com.kiwi.util.Constants.CMD_EXT;

import com.kiwi.dto.TCPRequest;
import com.kiwi.dto.TCPResponse;
import com.kiwi.exception.RequestParsingException;
import java.io.InputStream;
import java.net.Socket;
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
        try {
            final InputStream is = socket.getInputStream();
            while (true) {
                final TCPRequest request = requestParser.parse(is);
                final TCPResponse response = requestDispatcher.dispatch(request);
                responseWriter.writeResponse(socket, response);
                if (CMD_EXT.equals(request.method())) {
                    break;
                }
            }

            socket.close();
        } catch (Exception ex) {
            log.log(Level.SEVERE, "Unexpected error on get input stream");
            throw new RequestParsingException("Unexpected exception", ex);
        }
    }
}
