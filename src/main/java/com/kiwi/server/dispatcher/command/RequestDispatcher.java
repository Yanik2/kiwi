package com.kiwi.server.dispatcher.command;

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
import static com.kiwi.server.util.ServerConstants.OK_MESSAGE;

import com.kiwi.observability.MethodMetrics;
import com.kiwi.observability.MetricsProvider;
import com.kiwi.persistent.Storage;
import com.kiwi.server.Method;
import com.kiwi.server.dto.TCPRequest;
import com.kiwi.server.dto.TCPResponse;
import java.util.Collections;
import java.util.EnumMap;
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
        final var expireCommandHandler = new ExpireCommandHandler(storage);
        final var ttlCommandHandler = new TtlCommandHandler(storage);
        final var commands = new EnumMap<Method, RequestCommandHandler>(Method.class);

        commands.put(GET, new GetCommandHandler(storage));
        commands.put(SET, new SetCommandHandler(storage));
        commands.put(DEL, new DeleteCommandHandler(storage));
        commands.put(EXT, new ExitCommandHandler());
        commands.put(INF, new InfoCommandHandler(metricsProvider));
        commands.put(PING, new PingCommandHandler());
        commands.put(EXPIRE, expireCommandHandler);
        commands.put(PEXPIRE, expireCommandHandler);
        commands.put(PERSIST, new PersistCommandHandler(storage));
        commands.put(TTL, ttlCommandHandler);
        commands.put(PTTL, ttlCommandHandler);


        return new RequestDispatcher(metrics, Collections.unmodifiableMap(commands));
    }

    public TCPResponse dispatch(TCPRequest request) {
        final var result = commands.get(request.getMethod()).handle(request);
        metrics.onRequest(request.getMethod());
        return new TCPResponse(result, OK_MESSAGE, true);
    }
}
