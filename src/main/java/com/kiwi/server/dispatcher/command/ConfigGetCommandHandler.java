package com.kiwi.server.dispatcher.command;

import com.kiwi.config.ConfigModule;
import com.kiwi.config.domain.Config;
import com.kiwi.server.context.ConnectionContext;
import com.kiwi.server.dispatcher.OperationResult;
import com.kiwi.server.request.model.ConfigRequest;
import com.kiwi.server.request.model.TCPRequest;
import com.kiwi.server.response.model.ConfigResponse;

import java.util.Collections;
import java.util.Map;

import static com.kiwi.config.util.ConfigConstants.EVICTION_POLICY;
import static com.kiwi.config.util.ConfigConstants.MEMORY_MAX_BYTES;
import static com.kiwi.config.util.ConfigConstants.METRICS_ENABLED;
import static com.kiwi.config.util.ConfigConstants.SERVER_BACKLOG;
import static com.kiwi.config.util.ConfigConstants.SERVER_MAX_CLIENTS;
import static com.kiwi.config.util.ConfigConstants.SERVER_PORT;
import static com.kiwi.config.util.ConfigConstants.SOCKET_TIMEOUT;
import static com.kiwi.config.util.ConfigConstants.TTL_BACKOFF_MAX_MS;
import static com.kiwi.config.util.ConfigConstants.TTL_SAMPLER_PERIOD_MS;
import static com.kiwi.config.util.ConfigConstants.TTL_SAMPLE_BATCH;

public class ConfigGetCommandHandler implements RequestCommandHandler {
    @Override
    public OperationResult handle(TCPRequest request, ConnectionContext context) {
        final var parsedRequest = (ConfigRequest) request;
        final var configHolder = ConfigModule.getConfigurationHolder();

        return new OperationResult(getConfigResponse(parsedRequest.getConfigKey(), configHolder.getConfig()), true);
    }

    private ConfigResponse getConfigResponse(String configKey, Config config) {
        return switch(configKey) {
            case "*" -> new ConfigResponse(Map.of(
                    SERVER_PORT, config.port(),
                    SERVER_BACKLOG, config.backlog(),
                    SERVER_MAX_CLIENTS, config.maxClients(),
                    SOCKET_TIMEOUT, config.soTimeoutMillis(),
                    METRICS_ENABLED, config.metricsEnabled()
            ));
            case SERVER_PORT -> new ConfigResponse(Map.of(configKey, config.port()));
            case SERVER_BACKLOG -> new ConfigResponse(Map.of(configKey, config.backlog()));
            case SERVER_MAX_CLIENTS -> new ConfigResponse(Map.of(configKey, config.maxClients()));
            case SOCKET_TIMEOUT -> new ConfigResponse(Map.of(configKey, config.soTimeoutMillis()));
            case METRICS_ENABLED -> new ConfigResponse(Map.of(configKey, config.metricsEnabled()));
            case TTL_SAMPLER_PERIOD_MS -> new ConfigResponse(Map.of(configKey, config.ttlSamplerPeriodMs()));
            case TTL_SAMPLE_BATCH -> new ConfigResponse(Map.of(configKey, config.ttlSampleBatch()));
            case TTL_BACKOFF_MAX_MS -> new ConfigResponse(Map.of(configKey, config.ttlBackoffMaxMs()));
            case MEMORY_MAX_BYTES -> new ConfigResponse(Map.of(configKey, config.memoryMaxBytes()));
            case EVICTION_POLICY -> new ConfigResponse(Map.of(configKey, config.evictionPolicy().getValue()));
            default -> new ConfigResponse(Collections.emptyMap());
        };
    }
}
