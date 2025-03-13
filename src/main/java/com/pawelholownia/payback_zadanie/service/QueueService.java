package com.pawelholownia.payback_zadanie.service;

import com.pawelholownia.payback_zadanie.model.Request;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class QueueService {
    private static final Logger log = LoggerFactory.getLogger(QueueService.class);

    private final BlockingQueue<Request> requestQueue = new LinkedBlockingQueue<>();
    private final RequestProcessorService requestProcessorService;

    private final int MIN_THREADS = 1;
    private final int MAX_THREADS = 10;
    private final int QUEUE_SIZE_PER_THREAD = 5;
    private final long SCALING_CHECK_INTERVAL_MS = 5000;

    private ExecutorService consumerExecutor;
    private final AtomicInteger activeConsumers = new AtomicInteger(0);
    private volatile boolean running = true;
    private ScheduledExecutorService scalingExecutor;

    public QueueService(RequestProcessorService requestProcessorService) {
        this.requestProcessorService = requestProcessorService;
    }

    @PostConstruct
    public void initialize() {
        consumerExecutor = Executors.newCachedThreadPool(r -> {
            Thread t = new Thread(r);
            t.setDaemon(true);
            t.setName("Queue-Consumer-" + activeConsumers.getAndIncrement());
            return t;
        });

        for (int i = 0; i < MIN_THREADS; i++) {
            startNewConsumer();
        }

        scalingExecutor = Executors.newSingleThreadScheduledExecutor();
        scalingExecutor.scheduleAtFixedRate(
                this::evaluateThreadPoolSize,
                SCALING_CHECK_INTERVAL_MS,
                SCALING_CHECK_INTERVAL_MS,
                TimeUnit.MILLISECONDS
        );

        log.info("Queue service initialized with auto-scaling enabled");
    }

    private void startNewConsumer() {
        consumerExecutor.submit(this::consumeRequests);
    }

    private void evaluateThreadPoolSize() {
        int currentThreads = activeConsumers.get();
        int queueSize = requestQueue.size();
        int desiredThreads = Math.max(MIN_THREADS, Math.min(MAX_THREADS,
                queueSize / QUEUE_SIZE_PER_THREAD + 1));

        if (desiredThreads > currentThreads && currentThreads < MAX_THREADS) {
            int threadsToAdd = Math.min(desiredThreads - currentThreads, MAX_THREADS - currentThreads);
            log.info("Scaling up queue consumers by {} threads", threadsToAdd);

            for (int i = 0; i < threadsToAdd; i++) {
                startNewConsumer();
            }
        }
    }

    private void consumeRequests() {
        log.info("Consumer thread started: {}", Thread.currentThread().getName());

        while (running) {
            try {
                Request request = requestQueue.poll(1, TimeUnit.SECONDS);
                if (request != null) {
                    requestProcessorService.processRequest(request);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                log.error("Error processing request", e);
            }
        }

        activeConsumers.decrementAndGet();
    }

    public void addRequest(Request request) {
        requestQueue.add(request);
        log.debug("Added request to queue: {}", request);
    }

    @PreDestroy
    public void shutdown() {
        running = false;
        scalingExecutor.shutdownNow();
        consumerExecutor.shutdownNow();
        log.info("Queue service shut down");
    }
}