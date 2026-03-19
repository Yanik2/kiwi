package com.kiwi.server.factory;

import com.kiwi.concurrency.factory.ConcurrencyContainer;
import com.kiwi.observability.factory.ObservabilityContainer;
import com.kiwi.persistent.factory.PersistentContainer;
import com.kiwi.server.hook.ShutdownHook;
import com.kiwi.server.backpressure.BackPressureGate;
import com.kiwi.server.context.ConnectionRegistry;
import com.kiwi.server.request.ConnectionReader;
import com.kiwi.server.request.RequestHandler;
import com.kiwi.server.dispatcher.RequestDispatcher;
import com.kiwi.server.validator.BaseRequestValidator;
import com.kiwi.server.response.ResponseWriter;
import com.kiwi.server.accept.TCPServer;
import com.kiwi.server.parsing.BinaryRequestParser;

import static com.kiwi.config.properties.Properties.SERVER_THREAD_POOL_EXECUTOR_NAME;
import static com.kiwi.config.properties.Properties.SERVER_THREAD_POOL_NAME;

public class ServerModule {

    public static ServerContainer create(ObservabilityContainer observabilityContainer, PersistentContainer storageContainer,
                                         ConcurrencyContainer concurrencyContainer) {
        final var observabilityRequestHandler = observabilityContainer.metricsProvider();
        final var methodMetrics = observabilityContainer.methodMetrics();
        final var dispatcher =
                RequestDispatcher.create(observabilityRequestHandler, methodMetrics, storageContainer.storageFacade());
        final var requestMetrics = observabilityContainer.requestMetrics();
        final var requestValidator = new BaseRequestValidator();
        final var connectionRegistry = new ConnectionRegistry();
        final var parser = new BinaryRequestParser();
        final var requestHandler = new RequestHandler(dispatcher, requestValidator, requestMetrics);
        final var connectionReader = new ConnectionReader(
                parser,
                requestMetrics,
                concurrencyContainer.executors().get(SERVER_THREAD_POOL_EXECUTOR_NAME),
                requestHandler,
                connectionRegistry
        );
        final var responseWriter = new ResponseWriter();
        final var backPressureGate = new BackPressureGate(
                concurrencyContainer.executors().get(SERVER_THREAD_POOL_EXECUTOR_NAME),
                observabilityContainer.threadPoolMetrics().get(SERVER_THREAD_POOL_NAME)
        );
        final var tcpServer = new TCPServer(
                connectionReader, responseWriter, requestMetrics, backPressureGate, connectionRegistry
        );
        final var shutdownHook =
                new ShutdownHook(tcpServer, concurrencyContainer.executors().values(), connectionRegistry);

        return new ServerContainer(
                tcpServer,
                concurrencyContainer.executors().values(),
                shutdownHook
        );
    }

}
