package com.kiwi.server.request;

import com.kiwi.exception.protocol.ProtocolException;
import com.kiwi.observability.metrics.RequestMetrics;
import com.kiwi.server.context.ConnectionContext;
import com.kiwi.server.dispatcher.RequestDispatcher;
import com.kiwi.server.request.model.TCPRequest;
import com.kiwi.server.response.model.TCPResponse;
import com.kiwi.server.validator.RequestValidator;

import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static com.kiwi.server.util.ServerConstants.ERROR_MESSAGE;

public class RequestHandler {
    private static final Logger log = Logger.getLogger(RequestHandler.class.getName());

    private final RequestDispatcher requestDispatcher;
    private final RequestValidator requestValidator;
    private final RequestMetrics requestMetrics;

    public RequestHandler(RequestDispatcher requestDispatcher,
                          RequestValidator requestValidator,
                          RequestMetrics requestMetrics) {
        this.requestDispatcher = requestDispatcher;
        this.requestValidator = requestValidator;
        this.requestMetrics = requestMetrics;
    }

    public void handle(TCPRequest request, ConnectionContext connectionContext) {
        final var validationResult = requestValidator.validate(request);
        if (validationResult.errors().isEmpty()) {
            final var validatedRequest = validationResult.request();

            TCPResponse result;
            // PROBABLY BETTER CHOICE TO JUST RETURN TCP RESPONSE, AND EXCEPTION CATCH ON LOWER LEVEL
            try {
                result = requestDispatcher.dispatch(validatedRequest, connectionContext);
            } catch (Exception ex) {
                log.severe("Error in processing request with id: [" + request.getRequestId() + "], " + ex.getMessage());
                result = new TCPResponse(request.getRequestId(), ERROR_MESSAGE, false);
            }
            connectionContext.addResponse(result);
        } else {
            onValidationError(validationResult.errors(), connectionContext, request);
        }
    }

    public void reject(TCPRequest request, ConnectionContext context) {
        log.severe("Request is rejected: " + context.connectionId());
        context.addResponse(new TCPResponse(request.getRequestId(), ERROR_MESSAGE, false));
        requestMetrics.onRefuse();
        context.close();
    }

    private void onValidationError(List<ProtocolException> errors, ConnectionContext context, TCPRequest request) {
        final var errorMessages = errors.stream()
                .map(Throwable::getMessage)
                .collect(Collectors.joining("[", ",", "]"));
        log.severe("Request on connection: [" + context.connectionId() + "] is rejected. Errors: " + errorMessages);
        for (ProtocolException ex : errors) {
            requestMetrics.onProtoError(ex.getProtocolErrorCode());
        }
        context.addResponse(new TCPResponse(request.getRequestId(), ERROR_MESSAGE, false));
        context.close();
    }
}
