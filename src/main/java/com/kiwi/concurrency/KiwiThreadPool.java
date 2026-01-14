package com.kiwi.concurrency;

import com.kiwi.concurrency.exception.KiwiGeneralException;
import com.kiwi.observability.ThreadPoolMetrics;

import java.util.Map;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class KiwiThreadPool {
    private static final Logger log = Logger.getLogger(KiwiThreadPool.class.getSimpleName());
    private static final String WORKER_NAME_PREFIX = "-thread-pool-worker-";

    private final int size;
    private final Map<String, Worker> workers = new ConcurrentHashMap<>();
    private final BlockingQueue<Runnable> queue;
    private final Map<String, Thread> threads = new ConcurrentHashMap<>();
    private final ThreadFactory threadFactory;
    private final String name;
    private final ThreadPoolMetrics metrics;
    private volatile boolean isRunning;

    public KiwiThreadPool(ThreadFactory threadFactory, String threadPoolName, int poolSize, int queueSize,
                          ThreadPoolMetrics metrics) {
        this.threadFactory = threadFactory;
        this.size = poolSize;
        this.name = threadPoolName;
        this.queue = new ArrayBlockingQueue<>(queueSize);
        this.metrics = metrics;
        createThreads();
    }

    public void start() {
        for (Map.Entry<String, Thread> entry : threads.entrySet()) {
            entry.getValue().start();
        }
        this.isRunning = true;
        metrics.onWorkersMax(workers.size());
    }

    public void stop() {
        this.isRunning = false;
        for (Map.Entry<String, Worker> entry : workers.entrySet()) {
           entry.getValue().isRunning = false;
        }
    }

    public String getName() {
        return name;
    }

    private Consumer<String> onWorkerError() {
        return errorWorkerName -> {
            metrics.setWorkersActive(-1);
            final var worker = workers.get(errorWorkerName);
            if (worker.isRunning) {
                final var thread = threads.get(errorWorkerName);
                if (!thread.isInterrupted()) {
                    thread.interrupt();
                }
                worker.isRunning = false;
            }

            if (this.isRunning) {
                final var newThread = threadFactory.newThread(worker);
                threads.put(worker.name, newThread);
                newThread.start();
                worker.isRunning = true;
            }
        };
    }

    private Consumer<String> onWorkerDone() {
        return workerName -> {
            metrics.setWorkersActive(-1);
            final var worker = workers.get(workerName);
            if (worker.isRunning) {
                worker.isRunning = false;
            }

            if (this.isRunning) {
                // unknown situation
                log.severe("Worker: [" + workerName + "] is done, but thread pool is still running");
            }
        };
    }

    private static class Worker implements Runnable {
        private final Logger log;
        private final BlockingQueue<Runnable> queue;
        private final String name;
        private final Consumer<String> onError;
        private final ThreadPoolMetrics metrics;
        private final Consumer<String> onWorkerDone;

        private volatile boolean isRunning = false;

        public Worker(BlockingQueue<Runnable> queue, String name, Consumer<String> onError, ThreadPoolMetrics metrics,
                      Consumer<String> onWorkerDone) {
            this.queue = queue;
            this.name = name;
            this.log = Logger.getLogger(Worker.class.getSimpleName() + "-" + name);
            this.onError = onError;
            this.metrics = metrics;
            this.onWorkerDone = onWorkerDone;
        }

        @Override
        public void run() {
            this.isRunning = true;
            try {
                while (true) {
                    if (!isRunning && queue.isEmpty()) {
                        log.info("Thread worker: [" + this.name + "] stopped");
                        break;
                    }
                    final var task = queue.poll(100, TimeUnit.MILLISECONDS);
                    if (task != null) {
                        metrics.onQueueSize(-1);
                        metrics.setWorkersActive(1);
                        task.run();
                        metrics.setWorkersActive(-1);
                        metrics.onTaskCompleted();
                    }
                }
            } catch (KiwiGeneralException ex) {
                log.severe("Task execution failed, worker [%s], error: %s".formatted(this.name, ex.getMessage()));
                metrics.onTaskCompleted();
            } catch (Exception ex) {
                log.severe("Thread [%s] for worker [%s] was interrupted with exception: %s".formatted(
                        Thread.currentThread().getName(), this.name, ex.getMessage()));
                this.isRunning = false;
                onError.accept(this.name);
            }
            if (!isRunning) {
                onWorkerDone.accept(this.name);
            }
        }

    }

    public boolean submit(Runnable task, int timeout) {
        try {
            if (this.isRunning) {
                final var isAccepted = queue.offer(task, timeout, TimeUnit.SECONDS);
                updateMetrics(isAccepted);
                return isAccepted;
            } else {
                log.info("Thread pool: [" + this.name + "] is not running and cannot accept tasks");
                return false;
            }
        } catch (Exception ex) {
            log.log(Level.SEVERE, "Couldn't accept task: " + ex.getMessage() + ". Task will be rejected. "
            + "ThreadPool: [" + this.name + "]");
            return false;
        }
    }

    private void updateMetrics(boolean isAccepted) {
        if (isAccepted) {
            metrics.onTaskEnqueued();
            metrics.onQueueSize(1);
        } else {
            metrics.onTaskRejected();
        }
    }

    private void createThreads() {
        for (int i = 0; i < size; i++) {
            final var worker =
                    new Worker(queue, this.name + WORKER_NAME_PREFIX + i, onWorkerError(), metrics, onWorkerDone());
            final var thread = threadFactory.newThread(worker);
            workers.put(worker.name, worker);
            threads.put(worker.name, thread);
        }
    }

}
