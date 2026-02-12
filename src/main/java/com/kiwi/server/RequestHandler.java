package com.kiwi.server;

import static com.kiwi.server.Method.DEL;
import static com.kiwi.server.Method.EXPIRE;
import static com.kiwi.server.Method.EXT;
import static com.kiwi.server.Method.GET;
import static com.kiwi.server.Method.INF;
import static com.kiwi.server.Method.PERSIST;
import static com.kiwi.server.Method.PEXPIRE;
import static com.kiwi.server.Method.PING;
import static com.kiwi.server.Method.PTTL;
import static com.kiwi.server.Method.SET;
import static com.kiwi.server.Method.TTL;
import static com.kiwi.server.util.ServerConstants.ERROR_MESSAGE;

import com.kiwi.server.dispatcher.command.RequestDispatcher;
import com.kiwi.server.dto.ParsedRequest;
import com.kiwi.server.dto.TCPResponse;
import com.kiwi.exception.protocol.ProtocolException;
import com.kiwi.observability.RequestMetrics;
import com.kiwi.server.validator.ExpireValidator;
import com.kiwi.server.validator.NoOpValidator;
import com.kiwi.server.validator.RequestValidator;
import java.io.InputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RequestHandler {
    private static final Logger log = Logger.getLogger(RequestHandler.class.getSimpleName());
    //TODO will be moved to properties in phase 5
    private static final int MAX_CLIENTS = 10000000;

    private final RequestDispatcher requestDispatcher;
    private final BinaryRequestParser requestParser;
    private final ResponseWriter responseWriter;
    private final RequestMetrics metrics;
    private final Map<Method, RequestValidator> requestValidators;

    private RequestHandler(RequestDispatcher requestDispatcher, BinaryRequestParser requestParser,
                          ResponseWriter responseWriter, RequestMetrics metrics,
                          Map<Method, RequestValidator> requestValidators) {
        this.requestDispatcher = requestDispatcher;
        this.requestParser = requestParser;
        this.responseWriter = responseWriter;
        this.metrics = metrics;
        this.requestValidators = requestValidators;
    }

    public static RequestHandler create(RequestDispatcher requestDispatcher, BinaryRequestParser requestParser,
                                        ResponseWriter responseWriter, RequestMetrics metrics) {
        final var expireValidator = new ExpireValidator();
        final var validators = new EnumMap<Method, RequestValidator>(Method.class);

        validators.put(GET, NoOpValidator.getInstance());
        validators.put(SET, NoOpValidator.getInstance());
        validators.put(DEL, NoOpValidator.getInstance());
        validators.put(EXT, NoOpValidator.getInstance());
        validators.put(INF, NoOpValidator.getInstance());
        validators.put(PING, NoOpValidator.getInstance());
        validators.put(EXPIRE, expireValidator);
        validators.put(PEXPIRE, expireValidator);
        validators.put(PERSIST, NoOpValidator.getInstance());
        validators.put(TTL, NoOpValidator.getInstance());
        validators.put(PTTL, NoOpValidator.getInstance());

        return new RequestHandler(requestDispatcher, requestParser, responseWriter, metrics,
            Collections.unmodifiableMap(validators));
    }

    public void handle(Socket socket) {
        if (metrics.getCurrentClients() < MAX_CLIENTS) {
            acceptConnection(socket);
        } else {
            refuseConnection(socket);
        }
    }

    public void refuse(Socket socket) {
        refuseConnection(socket);
    }

    private void acceptConnection(Socket socket) {
        metrics.onAccept();
        try (socket) {
            try {
                final InputStream is = socket.getInputStream();
                while (true) {
                    final var isWrapper = new InputStreamWrapper(is);
                    final ParsedRequest request = requestParser.parse(isWrapper);
                    final var validatedRequest = requestValidators.getOrDefault(request.getMethod(),
                        NoOpValidator.getInstance()).validate(request);
                    metrics.onParse(isWrapper.getCounter());
                    final TCPResponse response = requestDispatcher.dispatch(validatedRequest);
                    final var writeResult = responseWriter.writeResponse(socket, response);
                    metrics.onWrite(writeResult.writtenBytes());
                    if (EXT.equals(validatedRequest.getMethod())) {
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
