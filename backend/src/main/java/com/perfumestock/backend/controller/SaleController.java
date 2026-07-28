package com.perfumestock.backend.controller;

import com.perfumestock.backend.dto.SaleRequest;
import com.perfumestock.backend.dto.SaleResponse;
import com.perfumestock.backend.dto.ReversalRequest;
import com.perfumestock.backend.service.SaleService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sales")
public class SaleController {

    private final SaleService saleService;

    public SaleController(SaleService saleService) {
        this.saleService = saleService;
    }

    @GetMapping
    public ResponseEntity<Page<SaleResponse>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return ResponseEntity.ok(saleService.list(PageRequest.of(page, size)));
    }

    @GetMapping("/recent")
    public ResponseEntity<List<SaleResponse>> recent() {
        return ResponseEntity.ok(saleService.recent());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SaleResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(saleService.getById(id));
    }

    @PostMapping
    @Operation(summary = "Record a sale and create an immutable sale event")
    public ResponseEntity<SaleResponse> record(@Valid @RequestBody SaleRequest req) {
        return ResponseEntity.ok(saleService.record(req));
    }

    @PostMapping("/{id}/reverse")
    @Operation(summary = "Reverse a sale by creating a compensating event")
    public ResponseEntity<SaleResponse> reverse(@PathVariable Long id,
                                                @RequestBody(required = false) ReversalRequest req) {
        return ResponseEntity.ok(saleService.reverse(id, req != null ? req.getReason() : null));
    }
}
