package com.kiwi.server.dispatcher;

import static com.kiwi.server.request.Method.DBSIZE;
import static com.kiwi.server.request.Method.DECR;
import static com.kiwi.server.request.Method.DECRBY;
import static com.kiwi.server.request.Method.DEL;
import static com.kiwi.server.request.Method.EXISTS;
import static com.kiwi.server.request.Method.EXPIRE;
import static com.kiwi.server.request.Method.EXT;
import static com.kiwi.server.request.Method.GET;
import static com.kiwi.server.request.Method.GETSET;
import static com.kiwi.server.request.Method.INCR;
import static com.kiwi.server.request.Method.INCRBY;
import static com.kiwi.server.request.Method.INF;
import static com.kiwi.server.request.Method.MGET;
import static com.kiwi.server.request.Method.MSET;
import static com.kiwi.server.request.Method.PERSIST;
import static com.kiwi.server.request.Method.PEXPIRE;
import static com.kiwi.server.request.Method.PING;
import static com.kiwi.server.request.Method.PTTL;
import static com.kiwi.server.request.Method.SET;
import static com.kiwi.server.request.Method.SETNX;
import static com.kiwi.server.request.Method.TTL;
import static com.kiwi.server.util.ServerConstants.ERROR_MESSAGE;
import static com.kiwi.server.util.ServerConstants.OK_MESSAGE;

import com.kiwi.observability.MethodMetrics;
import com.kiwi.observability.MetricsProvider;
import com.kiwi.observability.OperationErrorMetrics;
import com.kiwi.persistent.storage.Storage;
import com.kiwi.server.dispatcher.command.DbSizeCommandHandler;
import com.kiwi.server.dispatcher.command.DeleteCommandHandler;
import com.kiwi.server.dispatcher.command.ExistsCommandHandler;
import com.kiwi.server.dispatcher.command.ExitCommandHandler;
import com.kiwi.server.dispatcher.command.ExpireCommandHandler;
import com.kiwi.server.dispatcher.command.GetCommandHandler;
import com.kiwi.server.dispatcher.command.GetSetCommandHandler;
import com.kiwi.server.dispatcher.command.InfoCommandHandler;
import com.kiwi.server.dispatcher.command.MultiGetCommandHandler;
import com.kiwi.server.dispatcher.command.MultiSetCommandHandler;
import com.kiwi.server.dispatcher.command.NumericOperationCommandHandler;
import com.kiwi.server.dispatcher.command.PersistCommandHandler;
import com.kiwi.server.dispatcher.command.PingCommandHandler;
import com.kiwi.server.dispatcher.command.RequestCommandHandler;
import com.kiwi.server.dispatcher.command.SetCommandHandler;
import com.kiwi.server.dispatcher.command.SetNxCommandHandler;
import com.kiwi.server.dispatcher.command.TtlCommandHandler;
import com.kiwi.server.request.Method;
import com.kiwi.server.context.ConnectionContext;
import com.kiwi.server.request.model.TCPRequest;
import com.kiwi.server.response.model.TCPResponse;
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
                                           OperationErrorMetrics operationErrorMetrics,
                                           Storage storageFacade) {
        final var expireCommandHandler = new ExpireCommandHandler(storageFacade, operationErrorMetrics);
        final var ttlCommandHandler = new TtlCommandHandler(storageFacade);
        final var numericCommandHandler = new NumericOperationCommandHandler(storageFacade, operationErrorMetrics);
        final var commands = new EnumMap<Method, RequestCommandHandler>(Method.class);

        commands.put(GET, new GetCommandHandler(storageFacade));
        commands.put(SET, new SetCommandHandler(storageFacade));
        commands.put(DEL, new DeleteCommandHandler(storageFacade));
        commands.put(EXT, new ExitCommandHandler());
        commands.put(INF, new InfoCommandHandler(metricsProvider));
        commands.put(PING, new PingCommandHandler());
        commands.put(EXPIRE, expireCommandHandler);
        commands.put(PEXPIRE, expireCommandHandler);
        commands.put(PERSIST, new PersistCommandHandler(storageFacade, operationErrorMetrics));
        commands.put(TTL, ttlCommandHandler);
        commands.put(PTTL, ttlCommandHandler);
        commands.put(EXISTS, new ExistsCommandHandler(storageFacade));
        commands.put(SETNX, new SetNxCommandHandler(storageFacade));
        commands.put(GETSET, new GetSetCommandHandler(storageFacade));
        commands.put(INCR, numericCommandHandler);
        commands.put(INCRBY, numericCommandHandler);
        commands.put(DECR, numericCommandHandler);
        commands.put(DECRBY, numericCommandHandler);
        commands.put(MGET, new MultiGetCommandHandler(storageFacade));
        commands.put(MSET, new MultiSetCommandHandler(storageFacade));
        commands.put(DBSIZE, new DbSizeCommandHandler(storageFacade));

        return new RequestDispatcher(metrics, Collections.unmodifiableMap(commands));
    }

    public TCPResponse dispatch(TCPRequest request, ConnectionContext context) {
        final var result = commands.get(request.getMethod()).handle(request, context);
        metrics.onRequest(request.getMethod());
        return new TCPResponse(request.getRequestId(), result.value(),
                result.success() ? OK_MESSAGE : ERROR_MESSAGE, result.success());
    }
}
