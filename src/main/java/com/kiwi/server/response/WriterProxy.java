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

import static java.util.Comparator.comparingInt;

public class WriterProxy {
    private static final Logger log = Logger.getLogger(WriterProxy.class.getName());
    // will be moved to configuration
    private static final int RESPONSE_QUEUE_MAX_SIZE = 100;

    private final ResponseWriter responseWriter;
    private final OutputStream outputStream;
    private final RequestMetrics requestMetrics;
    private final AtomicInteger nextToWrite = new AtomicInteger(1);
    private Thread responseWriterThread;
    private final ReentrantLock lock;
    private final Condition hasElements;

    private final Queue<TCPResponse> responseQueue = new PriorityQueue<>(comparingInt(TCPResponse::requestId));

    private volatile boolean isActive;
    private volatile boolean drainMode;

    public WriterProxy(ResponseWriter responseWriter, OutputStream outputStream, RequestMetrics requestMetrics) {
        this.responseWriter = responseWriter;
        this.outputStream = outputStream;
        this.requestMetrics = requestMetrics;
        this.responseWriterThread = new Thread(writeResponse());
        this.lock = new ReentrantLock();
        this.hasElements = this.lock.newCondition();
        this.isActive = true;
        this.responseWriterThread.start();
    }

    public boolean addResponse(TCPResponse response) {
        lock.lock();
        try {
            if (isActive && responseQueue.size() < RESPONSE_QUEUE_MAX_SIZE) {
                responseQueue.add(response);
                hasElements.signal();
                return true;
            } else {
                return false;
            }
        } finally {
            lock.unlock();
        }
    }

    public void stop(boolean drain) throws InterruptedException {
        this.isActive = false;
        if (drain) {
            drainMode = true;
        }
        this.responseWriterThread.interrupt();
        this.responseWriterThread.join();

    }

    private void onThreadFailure() {
        if (this.isActive) {
            this.responseWriterThread = new Thread(writeResponse());
            this.responseWriterThread.start();
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
                        requestMetrics.onWrite(writeResult.writtenBytes());
                        nextToWrite.incrementAndGet();
                    }
                } catch (Exception ex) {
                    log.warning("Writer proxy thread exception: " + ex.getMessage());
                    onThreadFailure();
                }
            }

            if (drainMode) {
                lock.lock();
                try {
                    while (!responseQueue.isEmpty()) {
                        final var response = responseQueue.poll();
                        final var writerResult = responseWriter.writeResponse(outputStream, response);
                        requestMetrics.onWrite(writerResult.writtenBytes());
                    }
                } finally {
                    lock.unlock();
                }
                drainMode = false;
            }
        };
    }
}
