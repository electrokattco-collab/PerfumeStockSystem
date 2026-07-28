package com.perfumestock.backend.controller;

import com.perfumestock.backend.dto.PaymentRequest;
import com.perfumestock.backend.dto.PaymentResponse;
import com.perfumestock.backend.dto.ReversalRequest;
import com.perfumestock.backend.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<PaymentResponse>> getByCustomer(@PathVariable Long customerId) {
        return ResponseEntity.ok(paymentService.getByCustomer(customerId));
    }

    @PostMapping
    @Operation(summary = "Record a payment and append a payment event")
    public ResponseEntity<PaymentResponse> record(@Valid @RequestBody PaymentRequest req) {
        return ResponseEntity.ok(paymentService.record(req));
    }

    @PostMapping("/{id}/reverse")
    @Operation(summary = "Reverse a payment by creating a compensating event")
    public ResponseEntity<PaymentResponse> reverse(@PathVariable Long id,
                                                   @RequestBody(required = false) ReversalRequest req) {
        return ResponseEntity.ok(paymentService.reverse(id, req != null ? req.getReason() : null));
    }
}
