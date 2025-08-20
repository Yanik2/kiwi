package com.kiwi.server;

import static com.kiwi.util.Constants.CMD_DEL;
import static com.kiwi.util.Constants.CMD_EXT;
import static com.kiwi.util.Constants.CMD_GET;
import static com.kiwi.util.Constants.CMD_SET;

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
    private static final String ERROR_MESSAGE = "ERROR";

    private final DataProcessor dataProcessor;

    public RequestDispatcher(DataProcessor dataProcessor) {
        this.dataProcessor = dataProcessor;
    }

    public TCPResponse dispatch(TCPRequest request) {
        return switch (request.method()) {
            case CMD_GET -> new TCPResponse(dataProcessor.getValue(request.key()), SUCCESS_MESSAGE);
            case CMD_SET -> {
                dataProcessor.processData(new DataRequest(request.key(), request.value()));
                yield new TCPResponse(SUCCESS_MESSAGE);
            }
            case CMD_DEL -> {
                dataProcessor.deleteValue(request.key());
                yield new TCPResponse(SUCCESS_MESSAGE);
            }
            case CMD_EXT -> new TCPResponse(SUCCESS_MESSAGE);
            default -> {
                final var errorMessage = "Unknown request method" + request.method();
                logger.log(Level.SEVERE, errorMessage);
                throw new UnknownMethodException(errorMessage);
            }
        };
    }
}
