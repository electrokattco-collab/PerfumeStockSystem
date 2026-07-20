package com.perfumestock.backend.controller;

import com.perfumestock.backend.entity.PaymentHistory;
import com.perfumestock.backend.service.PaymentService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'SALES_REP')")
@RequestMapping("/api/payments")
@Tag(name = "Payments", description = "Customer payment management")
public class PaymentController {
    private final PaymentService service;
    @Autowired public PaymentController(PaymentService service) { this.service = service; }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<PaymentHistory>> getByCustomer(@PathVariable Long customerId) {
        return ResponseEntity.ok(service.getByCustomer(customerId));
    }

    @PostMapping
    public ResponseEntity<PaymentHistory> recordPayment(
            @RequestBody Map<String, Object> body, Authentication auth) {
        Long customerId = Long.valueOf(body.get("customerId").toString());
        Long saleId = body.get("saleId") != null ? Long.valueOf(body.get("saleId").toString()) : null;
        BigDecimal amount = new BigDecimal(body.get("amount").toString());
        String method = body.getOrDefault("paymentMethod", "CASH").toString();
        String notes = body.getOrDefault("notes", "").toString();
        return ResponseEntity.ok(service.recordPayment(customerId, saleId, amount, method, notes, auth.getName()));
    }
}
