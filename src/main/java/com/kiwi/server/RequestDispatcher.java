package com.kiwi.server;

import com.kiwi.dto.DataRequest;
import com.kiwi.dto.TCPRequest;
import com.kiwi.dto.TCPResponse;
import com.kiwi.exception.UnknownMethodException;
import com.kiwi.processor.DataProcessor;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RequestDispatcher {
    private static final Logger logger = Logger.getLogger(RequestDispatcher.class.getSimpleName());
    private static final String SUCCESS_MESSAGE = "OK";
    private static final String ERROR_LOG_MESSAGE = "Unknows request method";

    private final DataProcessor dataProcessor;

    public RequestDispatcher(DataProcessor dataProcessor) {
        this.dataProcessor = dataProcessor;
    }

    public TCPResponse dispatch(TCPRequest request) {
        return switch (request.method()) {
            case GET -> new TCPResponse(dataProcessor.getValue(request.key()), SUCCESS_MESSAGE, true);
            case SET -> {
                dataProcessor.processData(new DataRequest(request.key(), request.value()));
                yield new TCPResponse(SUCCESS_MESSAGE);
            }
            case DEL -> {
                dataProcessor.deleteValue(request.key());
                yield new TCPResponse(SUCCESS_MESSAGE);
            }
            case EXT -> new TCPResponse(SUCCESS_MESSAGE);
            case UNKNOWN -> {
                logger.log(Level.SEVERE, ERROR_LOG_MESSAGE);
                throw new UnknownMethodException(ERROR_LOG_MESSAGE);
            }
        };
    }
}
