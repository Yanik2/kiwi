package com.kiwi.server.dispatcher.command;

import static com.kiwi.server.Method.DEL;
import static com.kiwi.server.Method.EXPIRE;
import static com.kiwi.server.Method.EXT;
import static com.kiwi.server.Method.GET;
import static com.kiwi.server.Method.INF;
import static com.kiwi.server.Method.PING;
import static com.kiwi.server.Method.SET;
import static com.kiwi.server.util.ServerConstants.OK_MESSAGE;

import com.kiwi.observability.MethodMetrics;
import com.kiwi.observability.MetricsProvider;
import com.kiwi.persistent.Storage;
import com.kiwi.server.Method;
import com.kiwi.server.dto.TCPRequest;
import com.kiwi.server.dto.TCPResponse;
import java.util.Map;

public class RequestDispatcher {

    private final MethodMetrics metrics;
    private final Map<Method, RequestCommandHandler> commands;

    private RequestDispatcher(MethodMetrics metrics,
                              Map<Method, RequestCommandHandler> commands) {
        this.metrics = metrics;
        this.commands = commands;
    }

    public static RequestDispatcher create(MetricsProvider metricsProvider,
                                           MethodMetrics metrics,
                                           Storage storage) {
        final var commands = Map.of(
            GET, new GetCommandHandler(storage),
            SET, new SetCommandHandler(storage),
            DEL, new DeleteCommandHandler(storage),
            EXT, new ExitCommandHandler(),
            INF, new InfoCommandHandler(metricsProvider),
            PING, new PingCommandHandler(),
            EXPIRE, new ExpireCommandHandler(storage)
        );
        return new RequestDispatcher(metrics, commands);
    }

    public TCPResponse dispatch(TCPRequest request) {
        final var result = commands.get(request.getMethod()).handle(request);
        metrics.onRequest(request.getMethod());
        return new TCPResponse(result, OK_MESSAGE, true);
    }
}
