package com.pawelholownia.payback_zadanie.service;

import com.pawelholownia.payback_zadanie.model.Request;
import com.pawelholownia.payback_zadanie.repository.RequestRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class RequestProcessorService {
    private static final Logger log = LoggerFactory.getLogger(RequestProcessorService.class);

    private final RequestRepository requestRepository;
    private final String FILE_PATH = "requests.txt";
    private final ReentrantLock fileLock = new ReentrantLock();

    public RequestProcessorService(RequestRepository requestRepository) {
        this.requestRepository = requestRepository;
    }

    //@Async("taskExecutor")
    @Async
    public void processRequest(Request request) {
        log.info("Thread {} processing request: {}", Thread.currentThread().getName(), request);
        // Simulate processing time using random
//        try {
//            Thread.sleep((long) (Math.random() * 1000));
//        } catch (InterruptedException e) {
//            log.error("Error processing request", e);
//        }

        switch (request.getType()) {
            case TYPE1:
                saveToDatabase(request);
                break;
            case TYPE2:
                rejectRequest(request);
                break;
            case TYPE3:
                saveToFile(request);
                break;
            case TYPE4:
                logToConsole(request);
                break;
            default:
                log.warn("Unknown request type: {}", request.getType());
        }
    }

    private void saveToDatabase(Request request) {
        log.info("Saving request to database: {}", request);
        requestRepository.save(request);
    }

    private void rejectRequest(Request request) {
        log.info("Request rejected: {}", request);
    }

    private void saveToFile(Request request) {
        log.info("Saving request to file: {}", request);
        fileLock.lock();
        try {
            Files.writeString(
                    Paths.get(FILE_PATH),
                    request.getContent() + System.lineSeparator(),
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND
            );
        } catch (IOException e) {
            log.error("Error saving request to file", e);
        } finally {
            fileLock.unlock();
        }
    }

    private void logToConsole(Request request) {
        log.info("Request content: {}", request.getContent());
    }
}