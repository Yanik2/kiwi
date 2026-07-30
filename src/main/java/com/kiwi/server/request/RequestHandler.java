package com.kiwi.server.request;

import com.kiwi.exception.protocol.ProtocolException;
import com.kiwi.log.KiwiLogger;
import com.kiwi.log.KiwiLoggerFactory;
import com.kiwi.log.RequestContext;
import com.kiwi.observability.metrics.RequestMetrics;
import com.kiwi.server.context.ConnectionContext;
import com.kiwi.server.dispatcher.OperationResult;
import com.kiwi.server.dispatcher.RequestDispatcher;
import com.kiwi.server.request.model.TCPRequest;
import com.kiwi.server.response.model.TCPResponse;
import com.kiwi.server.response.model.TCPResponseResult;
import com.kiwi.server.validator.RequestValidator;

import java.util.List;
import java.util.stream.Collectors;

import static com.kiwi.server.util.ServerConstants.ERROR_MESSAGE;
import static com.kiwi.server.util.ServerConstants.OK_MESSAGE;

public class RequestHandler {
    private static final KiwiLogger log = KiwiLoggerFactory.getLogger(RequestHandler.class.getName());

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
        request.getKiwiRequest().startExecution();
        final var validationResult = requestValidator.validate(request);
        if (validationResult.errors().isEmpty()) {
            final var validatedRequest = validationResult.request();

            OperationResult result;
            TCPResponse tcpResponse;
            // PROBABLY BETTER CHOICE TO JUST RETURN TCP RESPONSE, AND EXCEPTION CATCH ON LOWER LEVEL
            try {
                result = requestDispatcher.dispatch(validatedRequest, connectionContext);
                tcpResponse = new TCPResponse(
                        request.getRequestId(),
                        request.getKiwiRequest(),
                        request.getMethod(),
                        connectionContext.connectionId(),
                        result.value(),
                        result.success() ? OK_MESSAGE : ERROR_MESSAGE,
                        result.success(),
                        result.success() ? TCPResponseResult.SUCCESS : TCPResponseResult.OPERATION_ERROR);
            } catch (Exception ex) {
                log.error("Error in processing request", ex.getMessage(), connectionContext.connectionId(),
                        new RequestContext(request.getRequestId(), request.getMethod()));
                tcpResponse = new TCPResponse(request.getRequestId(),
                        request.getKiwiRequest(),
                        request.getMethod(),
                        ERROR_MESSAGE,
                        false,
                        connectionContext.connectionId(),
                        TCPResponseResult.OPERATION_ERROR
                );
            }
            connectionContext.addResponse(tcpResponse);
        } else {
            onValidationError(validationResult.errors(), connectionContext, request);
        }
    }

    public void reject(TCPRequest request, ConnectionContext context) {
        request.getKiwiRequest().startExecution();
        log.error("Request is rejected", context.connectionId(),
                new RequestContext(request.getRequestId(), request.getMethod()));
        context.addResponse(new TCPResponse(request.getRequestId(),
                request.getKiwiRequest(),
                request.getMethod(),
                ERROR_MESSAGE,
                false,
                context.connectionId(),
                TCPResponseResult.REJECTED
        ));
        requestMetrics.onRefuse();
        context.close();
    }

    private void onValidationError(List<ProtocolException> errors, ConnectionContext context, TCPRequest request) {
        final var errorMessages = errors.stream()
                .map(Throwable::getMessage)
                .collect(Collectors.joining("[", ",", "]"));
        log.error("Request is rejected", errorMessages, context.connectionId(),
                new RequestContext(request.getRequestId(), request.getMethod()));
        for (ProtocolException ex : errors) {
            requestMetrics.onProtoError(ex.getProtocolErrorCode());
        }
        context.addResponse(new TCPResponse(
                request.getRequestId(),
                request.getKiwiRequest(),
                request.getMethod(),
                ERROR_MESSAGE,
                false,
                context.connectionId(),
                TCPResponseResult.OPERATION_ERROR
        ));
        context.close();
    }
}
