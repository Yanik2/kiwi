package com.kiwi.server.response;

import com.kiwi.log.KiwiLogger;
import com.kiwi.log.KiwiLoggerFactory;
import com.kiwi.observability.metrics.RequestMetrics;
import com.kiwi.server.response.model.TCPResponse;

import java.io.OutputStream;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import static com.kiwi.config.properties.Properties.RESPONSE_QUEUE_MAX_SIZE;
import static com.kiwi.config.properties.Properties.THREAD_NAME_PREFIX;
import static com.kiwi.server.response.dto.WriteResponseStatus.OK;
import static java.util.Comparator.comparingInt;

public class WriterProxy {
    private static final KiwiLogger log = KiwiLoggerFactory.getLogger(WriterProxy.class.getName());

    private final ResponseWriter responseWriter;
    private final OutputStream outputStream;
    private final RequestMetrics requestMetrics;
    private final AtomicInteger nextToWrite = new AtomicInteger(1);
    private final Thread responseWriterThread;
    private final ReentrantLock lock;
    private final Condition hasElements;
    private final WriterLock writerLock;

    private final Queue<TCPResponse> responseQueue = new PriorityQueue<>(comparingInt(TCPResponse::requestId));

    private volatile boolean isActive;
    private volatile boolean drainMode;
    private volatile int lastResponseId = -1;

    private final long connectionId;

    public WriterProxy(ResponseWriter responseWriter, OutputStream outputStream, RequestMetrics requestMetrics,
                       WriterLock writerLock, long connectionId) {
        this.responseWriter = responseWriter;
        this.outputStream = outputStream;
        this.requestMetrics = requestMetrics;
        this.responseWriterThread = new Thread(writeResponse());
        this.responseWriterThread.setName(THREAD_NAME_PREFIX + "response-writer-" + connectionId);
        this.lock = new ReentrantLock();
        this.hasElements = this.lock.newCondition();
        this.isActive = true;
        this.responseWriterThread.start();
        this.writerLock = writerLock;
        this.connectionId = connectionId;
    }

    public void setLastResponseId(int id) {
        this.lastResponseId = id;
    }

    public boolean addResponse(TCPResponse response) {
        lock.lock();
        try {
            if (isActive && responseQueue.size() < RESPONSE_QUEUE_MAX_SIZE) {
                response.completeRequestExecution();
                responseQueue.add(response);
                hasElements.signal();
                requestMetrics.onPendingResponse(1);
                return true;
            } else {
                log.warn("Failed to add response to writer proxy", "Writer proxy active: [" + isActive + "], "
                        + "response queue size: [" + responseQueue.size()
                        + "], Max response queue size: [" + RESPONSE_QUEUE_MAX_SIZE + "]", connectionId);
                return false;
            }
        } finally {
            lock.unlock();
        }
    }

    public void stop(boolean drain) throws InterruptedException {
        if (!isActive) {
            return;
        }
        this.isActive = false;
        if (drain) {
            drainMode = true;
        }
        this.responseWriterThread.interrupt();
        this.responseWriterThread.join(10000);

        if (drainMode) {
            this.responseWriterThread.interrupt();
            this.drainMode = false;
            requestMetrics.onDrainTimeout();
        }
    }

    private Runnable writeResponse() {
        return () -> {
            while (isActive) {
                try {
                    TCPResponse response;
                    lock.lock();
                    try {
                        while (((response = responseQueue.peek()) == null
                                || response.requestId() != nextToWrite.get()) && isActive) {
                            hasElements.await();
                        }
                        responseQueue.poll();
                    } finally {
                        lock.unlock();
                    }

                    if (response != null) {
                        response.completeRequest();
                        if (isActive) {
                            final var writeResult = responseWriter.writeResponse(outputStream, response);
                            if (OK == writeResult.status()) {
                                requestMetrics.onWrite(writeResult.writtenBytes());
                                nextToWrite.incrementAndGet();
                                requestMetrics.onPendingResponse(-1);
                                writerLock.onResponse();
                                writerLock.notifyInflight();
                                if (lastResponseId == response.requestId()) {
                                    isActive = false;
                                }
                            } else {
                                isActive = false;
                            }
                        }
                    }
                } catch (Exception ex) {
                    if (ex instanceof InterruptedException && !isActive) {
                        log.info("Writer proxy interrupted and not active", ex.getMessage(), connectionId);
                    } else {
                        log.warn("Writer proxy thread error", ex.getMessage(), connectionId);
                    }
                }
            }

            if (drainMode) {
                lock.lock();
                try {
                    while (!responseQueue.isEmpty() && drainMode) {
                        final var response = responseQueue.poll();
                        response.completeRequest();
                        final var writerResult = responseWriter.writeResponse(outputStream, response);
                        requestMetrics.onPendingResponse(-1);
                        requestMetrics.onWrite(writerResult.writtenBytes());
                    }
                } finally {
                    lock.unlock();
                    drainMode = false;
                    writerLock.notifyInflight();
                    if (!responseQueue.isEmpty()) {
                        for (TCPResponse r : responseQueue) {
                            r.completeRequest();
                        }
                    }
                }
            }

            writerLock.notifyWriterDone();
        };
    }
}
