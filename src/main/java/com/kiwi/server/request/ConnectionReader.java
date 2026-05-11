package com.kiwi.server.request;

import com.kiwi.concurrency.KiwiThreadPoolExecutor;
import com.kiwi.concurrency.task.ConnectionTask;
import com.kiwi.exception.protocol.ProtocolException;
import com.kiwi.log.KiwiLogger;
import com.kiwi.log.KiwiLoggerFactory;
import com.kiwi.observability.metrics.RequestMetrics;
import com.kiwi.server.buffer.Cursor;
import com.kiwi.server.buffer.ReadBuffer;
import com.kiwi.server.context.ConnectionContext;
import com.kiwi.server.context.ConnectionRegistry;
import com.kiwi.server.request.model.ParsedRequest;
import com.kiwi.server.request.model.ParserResult;
import com.kiwi.server.request.model.TCPRequest;
import com.kiwi.server.response.model.TCPResponse;
import com.kiwi.server.parsing.BinaryRequestParser;

import java.net.SocketTimeoutException;

import static com.kiwi.server.request.Method.EXT;
import static com.kiwi.server.util.ServerConstants.ERROR_MESSAGE;

public class ConnectionReader {
    private static final KiwiLogger log = KiwiLoggerFactory.getLogger(ConnectionReader.class.getName());

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
        requestMetrics.onReaderThreadActive(1);
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
                    for (ParserResult<ParsedRequest> parserResult : parserResults) {
                        switch (parserResult.status()) {
                            case OK -> delegateTask(context, parserResult.value());
                            case NEED_MORE_DATA -> {
                            }
                            case ERROR -> throw parserResult.error();
                        }
                        if (context.isClosed()) {
                            break;
                        }
                    }
                }

            }
        } catch (SocketTimeoutException ex) {
            log.warn("Connection reader error", "Socket timed out", context.connectionId());
        } catch (ProtocolException ex) {
            onError(ex, context);
        } catch (Exception ex) {
            log.error("Error during request processing", ex.getMessage(), context.connectionId());
        } finally {
            context.close();
            connectionRegistry.unregister(context);
            requestMetrics.onParse(readBuffer.getReadBytes());
            requestMetrics.onClose();
            requestMetrics.onReaderThreadActive(-1);
        }
    }

    private void onError(ProtocolException ex, ConnectionContext context) {
        log.error("Error in protocol parsing", ex.getMessage(), context.connectionId());
        context.addResponse(new TCPResponse(context.getRequestId(), ERROR_MESSAGE, false, context.connectionId()));
        requestMetrics.onProtoError(ex.getProtocolErrorCode());
    }

    private void delegateTask(ConnectionContext context, TCPRequest tcpRequest) throws InterruptedException {
        if (EXT.equals(tcpRequest.getMethod())) {
            context.closeAfter(tcpRequest.getRequestId());
        }
        context.awaitInflight();
        final var task = new ConnectionTask(requestHandler, context, tcpRequest, 5);
        taskExecutor.submit(task);
        context.inflightRequest();

        if (EXT.equals(tcpRequest.getMethod())) {
            context.awaitWriterDone();
            context.close();
        }
    }
}
