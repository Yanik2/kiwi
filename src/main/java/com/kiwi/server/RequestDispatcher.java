package com.kiwi.server;

import static com.kiwi.server.util.ServerConstants.OK_MESSAGE;

import com.kiwi.dto.DataRequest;
import com.kiwi.server.dto.TCPRequest;
import com.kiwi.server.dto.TCPResponse;
import com.kiwi.exception.UnknownMethodException;
import com.kiwi.observability.ObservabilityRequestHandler;
import com.kiwi.processor.DataProcessor;
import com.kiwi.server.response.DataResponse;
import com.kiwi.server.response.ObservabilityResponse;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RequestDispatcher {
    private static final Logger logger = Logger.getLogger(RequestDispatcher.class.getSimpleName());
    private static final String ERROR_LOG_MESSAGE = "Unknows request method";

    private final DataProcessor dataProcessor;
    private final ObservabilityRequestHandler observabilityRequestHandler;

    public RequestDispatcher(DataProcessor dataProcessor,
                             ObservabilityRequestHandler observabilityRequestHandler) {
        this.dataProcessor = dataProcessor;
        this.observabilityRequestHandler = observabilityRequestHandler;
    }

    public TCPResponse dispatch(TCPRequest request) {
        return switch (request.method()) {
            case GET -> new TCPResponse(
                new DataResponse(dataProcessor.getValue(request.key())),
                OK_MESSAGE,
                true
            );
            case SET -> {
                dataProcessor.processData(new DataRequest(request.key(), request.value()));
                yield new TCPResponse(OK_MESSAGE);
            }
            case DEL -> {
                dataProcessor.deleteValue(request.key());
                yield new TCPResponse(OK_MESSAGE);
            }
            case EXT -> new TCPResponse(OK_MESSAGE);
            case INF -> new TCPResponse(
                new ObservabilityResponse(observabilityRequestHandler.getMetricsInfo()),
                OK_MESSAGE,
                true
            );
            case UNKNOWN -> {
                logger.log(Level.SEVERE, ERROR_LOG_MESSAGE);
                throw new UnknownMethodException(ERROR_LOG_MESSAGE);
            }
        };
    }
}
