package com.perfumestock.backend.controller;

import com.perfumestock.backend.dto.PurchaseRequest;
import com.perfumestock.backend.dto.PurchaseResponse;
import com.perfumestock.backend.dto.ReversalRequest;
import com.perfumestock.backend.service.PurchaseService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/purchases")
public class PurchaseController {

    private final PurchaseService purchaseService;

    public PurchaseController(PurchaseService purchaseService) {
        this.purchaseService = purchaseService;
    }

    @GetMapping
    public ResponseEntity<Page<PurchaseResponse>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return ResponseEntity.ok(purchaseService.list(PageRequest.of(page, size)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PurchaseResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(purchaseService.getById(id));
    }

    @PostMapping
    @Operation(summary = "Record a purchase receipt without updating stock")
    public ResponseEntity<PurchaseResponse> record(@Valid @RequestBody PurchaseRequest req) {
        return ResponseEntity.ok(purchaseService.record(req));
    }

    @PostMapping("/{id}/confirm")
    @Operation(summary = "Confirm a purchase and release stock into inventory")
    public ResponseEntity<PurchaseResponse> confirm(@PathVariable Long id) {
        return ResponseEntity.ok(purchaseService.confirm(id));
    }

    @PostMapping("/{id}/reverse")
    @Operation(summary = "Reverse a purchase by creating a compensating event")
    public ResponseEntity<PurchaseResponse> reverse(@PathVariable Long id,
                                                    @RequestBody(required = false) ReversalRequest req) {
        return ResponseEntity.ok(purchaseService.reverse(id, req != null ? req.getReason() : null));
    }
}
