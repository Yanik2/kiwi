package com.kiwi.server;

import static com.kiwi.server.util.ServerConstants.OK_MESSAGE;

import com.kiwi.dto.DataRequest;
import com.kiwi.dto.TCPRequest;
import com.kiwi.dto.TCPResponse;
import com.kiwi.exception.UnknownMethodException;
import com.kiwi.processor.DataProcessor;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RequestDispatcher {
    private static final Logger logger = Logger.getLogger(RequestDispatcher.class.getSimpleName());
    private static final String ERROR_LOG_MESSAGE = "Unknows request method";

    private final DataProcessor dataProcessor;

    public RequestDispatcher(DataProcessor dataProcessor) {
        this.dataProcessor = dataProcessor;
    }

    public TCPResponse dispatch(TCPRequest request) {
        return switch (request.method()) {
            case GET -> new TCPResponse(dataProcessor.getValue(request.key()), OK_MESSAGE, true);
            case SET -> {
                dataProcessor.processData(new DataRequest(request.key(), request.value()));
                yield new TCPResponse(OK_MESSAGE);
            }
            case DEL -> {
                dataProcessor.deleteValue(request.key());
                yield new TCPResponse(OK_MESSAGE);
            }
            case EXT -> new TCPResponse(OK_MESSAGE);
            case UNKNOWN -> {
                logger.log(Level.SEVERE, ERROR_LOG_MESSAGE);
                throw new UnknownMethodException(ERROR_LOG_MESSAGE);
            }
        };
    }
}
