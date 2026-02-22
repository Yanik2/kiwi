package com.kiwi.server;

import com.kiwi.exception.protocol.ProtocolException;
import com.kiwi.observability.RequestMetrics;
import com.kiwi.server.context.ConnectionContext;
import com.kiwi.server.dispatcher.command.RequestDispatcher;
import com.kiwi.server.dto.TCPRequest;
import com.kiwi.server.dto.TCPResponse;
import com.kiwi.server.validator.RequestValidator;

import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static com.kiwi.server.util.ServerConstants.ERROR_MESSAGE;

public class RequestHandler {
    private static final Logger log = Logger.getLogger(RequestHandler.class.getName());

    private final RequestDispatcher requestDispatcher;
    private final ResponseWriter responseWriter;
    private final RequestValidator requestValidator;
    private final RequestMetrics requestMetrics;

    public RequestHandler(RequestDispatcher requestDispatcher,
                          ResponseWriter responseWriter,
                          RequestValidator requestValidator,
                          RequestMetrics requestMetrics) {
        this.requestDispatcher = requestDispatcher;
        this.responseWriter = responseWriter;
        this.requestValidator = requestValidator;
        this.requestMetrics = requestMetrics;
    }

    public void handle(TCPRequest request, ConnectionContext connectionContext) {
        final var validationResult = requestValidator.validate(request);
        if (!validationResult.errors().isEmpty()) {
            onValidationError(validationResult.errors(), connectionContext);
        } else {
            final var validatedRequest = validationResult.request();
            final var result = requestDispatcher.dispatch(validatedRequest, connectionContext);
            final var writeResult = responseWriter.write(connectionContext, result);
            requestMetrics.onWrite(writeResult.writtenBytes());
        }
    }

    public void reject(TCPRequest request, ConnectionContext context) {
        log.severe("Request is rejected: " + context.connectionId());
        requestMetrics.onRefuse();
        context.close();
        requestMetrics.onClose();
    }

    private void onValidationError(List<ProtocolException> errors, ConnectionContext context) {
        final var errorMessages = errors.stream()
                .map(Throwable::getMessage)
                .collect(Collectors.joining("[", ",", "]"));
        log.severe("Request on connection: [" + context.connectionId() + "] is rejected. Errors: " + errorMessages);
        for (ProtocolException ex : errors) {
            requestMetrics.onProtoError(ex.getProtocolErrorCode());
        }
        final var writeResult = responseWriter.write(context, new TCPResponse(ERROR_MESSAGE, false));
        requestMetrics.onWrite(writeResult.writtenBytes());
        context.close();
        requestMetrics.onClose();
    }
}
