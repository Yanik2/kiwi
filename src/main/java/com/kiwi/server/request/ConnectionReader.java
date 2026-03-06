package com.kiwi.server.request;

import com.kiwi.concurrency.KiwiThreadPoolExecutor;
import com.kiwi.concurrency.task.ConnectionTask;
import com.kiwi.exception.protocol.ProtocolException;
import com.kiwi.observability.RequestMetrics;
import com.kiwi.server.buffer.Cursor;
import com.kiwi.server.buffer.ReadBuffer;
import com.kiwi.server.context.ConnectionContext;
import com.kiwi.server.context.ConnectionRegistry;
import com.kiwi.server.dto.TCPRequest;
import com.kiwi.server.response.model.TCPResponse;
import com.kiwi.server.parsing.BinaryRequestParser;

import java.net.SocketTimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.kiwi.server.util.ServerConstants.ERROR_MESSAGE;

public class ConnectionReader {
    private static final Logger log = Logger.getLogger(ConnectionReader.class.getName());

    private final BinaryRequestParser requestParser;
    private final RequestMetrics requestMetrics;
    private final KiwiThreadPoolExecutor taskExecutor;
    private final RequestHandler requestHandler;
    private final ConnectionRegistry connectionRegistry;

    public ConnectionReader(BinaryRequestParser binaryRequestParser,
                            RequestMetrics requestMetrics,
                            KiwiThreadPoolExecutor taskExecutor,
                            RequestHandler requestHandler,
                            ConnectionRegistry connectionRegistry) {
        this.requestParser = binaryRequestParser;
        this.requestMetrics = requestMetrics;
        this.taskExecutor = taskExecutor;
        this.requestHandler = requestHandler;
        this.connectionRegistry = connectionRegistry;
    }

    public void readConnection(ConnectionContext context) {
        final var readBuffer = new ReadBuffer();
        final var cursor = new Cursor(readBuffer);
        final var socket = context.socket();
        try {
            final var is = socket.getInputStream();
            while (!context.isClosed()) {
                context.awaitIfOverload();
                final var bytesRead = readBuffer.fill(is, context);
                if (bytesRead == -1) {
                    break;
                }
                cursor.reset();
                final var parserResults = requestParser.parse(cursor, context);
                if (!parserResults.isEmpty()) {
                    parserResults.forEach(parserResult -> {
                        switch (parserResult.status()) {
                            case OK -> delegateTask(context, parserResult.value());
                            case NEED_MORE_DATA -> {}
                            case ERROR -> throw parserResult.error();
                        }
                    });
                }

            }
        } catch (SocketTimeoutException ex) {
            log.log(Level.WARNING, "Socket timed out");
        } catch (ProtocolException ex) {
            onError(ex, context);
        } catch (Exception ex) {
            log.severe("Unexpected exception during request processing: " + ex.getMessage());
        } finally {
            context.close();
            connectionRegistry.unregister(context);
            requestMetrics.onParse(readBuffer.getReadBytes());
            requestMetrics.onClose();
        }
    }

    private void onError(ProtocolException ex, ConnectionContext context) {
        log.severe("Unexpected error in protocol parsing: " + ex.getMessage());
        context.addResponse(new TCPResponse(context.getRequestId(), ERROR_MESSAGE, false));
        requestMetrics.onProtoError(ex.getProtocolErrorCode());
    }

    private void delegateTask(ConnectionContext context, TCPRequest tcpRequest) {
        final var task = new ConnectionTask(requestHandler, context, tcpRequest, 5);
        taskExecutor.submit(task);
    }
}
