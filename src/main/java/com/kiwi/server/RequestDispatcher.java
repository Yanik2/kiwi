package com.kiwi.server;

import static com.kiwi.server.util.ServerConstants.OK_MESSAGE;

import com.kiwi.dto.DataRequest;
import com.kiwi.observability.MethodMetrics;
import com.kiwi.observability.ObservabilityRequestHandler;
import com.kiwi.processor.DataProcessor;
import com.kiwi.server.dto.TCPRequest;
import com.kiwi.server.dto.TCPResponse;
import com.kiwi.server.response.DataResponse;
import com.kiwi.server.response.ObservabilityResponse;

public class RequestDispatcher {

    private final DataProcessor dataProcessor;
    private final ObservabilityRequestHandler observabilityRequestHandler;
    private final MethodMetrics metrics;

    public RequestDispatcher(DataProcessor dataProcessor,
                             ObservabilityRequestHandler observabilityRequestHandler,
                             MethodMetrics metrics) {
        this.dataProcessor = dataProcessor;
        this.observabilityRequestHandler = observabilityRequestHandler;
        this.metrics = metrics;
    }

    public TCPResponse dispatch(TCPRequest request) {
        return switch (request.method()) {
            case GET -> {
                metrics.onGet();
                yield new TCPResponse(
                    new DataResponse(dataProcessor.getValue(request.key())), OK_MESSAGE, true);
            }
            case SET -> {
                metrics.onSet();
                dataProcessor.processData(new DataRequest(request.key(), request.value()));
                yield new TCPResponse(OK_MESSAGE);
            }
            case DEL -> {
                metrics.onDelete();
                dataProcessor.deleteValue(request.key());
                yield new TCPResponse(OK_MESSAGE);
            }
            case EXT -> {
                metrics.onExit();
                yield new TCPResponse(OK_MESSAGE);
            }
            case INF -> {
                metrics.onInfo();
                yield new TCPResponse(
                    new ObservabilityResponse(observabilityRequestHandler.getMetricsInfo()),
                    OK_MESSAGE,
                    true
                );
            }
        };
    }
}
