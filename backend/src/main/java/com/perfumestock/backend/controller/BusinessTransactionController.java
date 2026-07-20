package com.perfumestock.backend.controller;

import com.perfumestock.backend.entity.BusinessTransaction;
import com.perfumestock.backend.service.BusinessTransactionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/finance")
@Tag(name = "Business Finance", description = "Financial transactions and planning")
public class BusinessTransactionController {
    private final BusinessTransactionService service;
    @Autowired public BusinessTransactionController(BusinessTransactionService service) { this.service = service; }

    @GetMapping
    public ResponseEntity<List<BusinessTransaction>> getAll() { return ResponseEntity.ok(service.getAll()); }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<BusinessTransaction> create(@Valid @RequestBody BusinessTransaction t) { return ResponseEntity.ok(service.create(t)); }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Void> delete(@PathVariable Long id) { service.delete(id); return ResponseEntity.ok().build(); }

    @GetMapping("/summary")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Map<String, Object>> getSummary() { return ResponseEntity.ok(service.getFinancialSummary()); }

    @GetMapping("/range")
    public ResponseEntity<List<BusinessTransaction>> getByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return ResponseEntity.ok(service.getByDateRange(start, end));
    }
}
