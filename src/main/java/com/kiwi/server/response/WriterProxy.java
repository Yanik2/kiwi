package com.kiwi.server.response;

import com.kiwi.observability.RequestMetrics;
import com.kiwi.server.response.model.TCPResponse;

import java.io.OutputStream;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

import static com.kiwi.config.properties.Properties.RESPONSE_QUEUE_MAX_SIZE;
import static com.kiwi.server.response.dto.WriteResponseStatus.OK;
import static java.util.Comparator.comparingInt;

public class WriterProxy {
    private static final Logger log = Logger.getLogger(WriterProxy.class.getName());

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

    public WriterProxy(ResponseWriter responseWriter, OutputStream outputStream, RequestMetrics requestMetrics,
                       WriterLock writerLock) {
        this.responseWriter = responseWriter;
        this.outputStream = outputStream;
        this.requestMetrics = requestMetrics;
        this.responseWriterThread = new Thread(writeResponse());
        this.lock = new ReentrantLock();
        this.hasElements = this.lock.newCondition();
        this.isActive = true;
        this.responseWriterThread.start();
        this.writerLock = writerLock;
    }

    public void setLastResponseId(int id) {
        this.lastResponseId = id;
    }

    public boolean addResponse(TCPResponse response) {
        lock.lock();
        try {
            if (isActive && responseQueue.size() < RESPONSE_QUEUE_MAX_SIZE) {
                responseQueue.add(response);
                hasElements.signal();
                requestMetrics.onPendingResponse(1);
                return true;
            } else {
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
                } catch (Exception ex) {
                    if (ex instanceof InterruptedException && !isActive) {
                        log.info("Writer proxy interrupted and not active");
                    } else {
                        log.warning("Writer proxy thread exception: " + ex.getMessage());
                    }
                }
            }

            if (drainMode) {
                System.out.println("In drain mode");
                lock.lock();
                try {
                    while (!responseQueue.isEmpty() && drainMode) {
                        final var response = responseQueue.poll();
                        final var writerResult = responseWriter.writeResponse(outputStream, response);
                        requestMetrics.onPendingResponse(-1);
                        requestMetrics.onWrite(writerResult.writtenBytes());
                    }
                } finally {
                    lock.unlock();
                    drainMode = false;
                    writerLock.notifyInflight();
                }
            }

            System.out.println("Left drain mode");
            writerLock.notifyWriterDone();
        };
    }
}
