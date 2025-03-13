package com.pawelholownia.payback_zadanie.controller;

import com.pawelholownia.payback_zadanie.model.Request;
import com.pawelholownia.payback_zadanie.service.QueueService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/requests")
public class RequestController {
    private final QueueService queueService;

    public RequestController(QueueService queueService) {
        this.queueService = queueService;
    }

    @PostMapping
    public ResponseEntity<String> submitRequest(@RequestBody Request request) {
        queueService.addRequest(request);
        return ResponseEntity.accepted().body("Request queued for processing");
    }
}