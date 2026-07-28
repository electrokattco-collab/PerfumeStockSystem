package com.perfumestock.backend.controller;

import com.perfumestock.backend.dto.CustomerRequest;
import com.perfumestock.backend.dto.CustomerBalanceResponse;
import com.perfumestock.backend.dto.CustomerLedgerEntry;
import com.perfumestock.backend.dto.CustomerResponse;
import com.perfumestock.backend.dto.CustomerStatementResponse;
import com.perfumestock.backend.entity.BusinessEventType;
import com.perfumestock.backend.service.CustomerService;
import com.perfumestock.backend.service.CustomerLedgerService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerService customerService;
    private final CustomerLedgerService customerLedgerService;

    public CustomerController(CustomerService customerService, CustomerLedgerService customerLedgerService) {
        this.customerService = customerService;
        this.customerLedgerService = customerLedgerService;
    }

    @GetMapping
    public ResponseEntity<Page<CustomerResponse>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return ResponseEntity.ok(customerService.list(PageRequest.of(page, size)));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<CustomerResponse>> search(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return ResponseEntity.ok(customerService.search(q, PageRequest.of(page, size)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomerResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(customerService.getById(id));
    }

    @GetMapping("/debtors")
    public ResponseEntity<List<CustomerResponse>> debtors() {
        return ResponseEntity.ok(customerService.getDebtors());
    }

    @GetMapping("/{id}/ledger")
    @Operation(summary = "Get a paged customer ledger")
    public ResponseEntity<Page<CustomerLedgerEntry>> ledger(
            @PathVariable Long id,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(required = false) BusinessEventType transactionType,
            @PageableDefault(size = 50) Pageable pageable) {
        return ResponseEntity.ok(customerLedgerService.getLedgerPage(id, startDate, endDate, transactionType, pageable));
    }

    @GetMapping("/{id}/balance")
    @Operation(summary = "Get the derived outstanding balance for a customer")
    public ResponseEntity<CustomerBalanceResponse> balance(@PathVariable Long id) {
        return ResponseEntity.ok(customerLedgerService.getBalance(id));
    }

    @GetMapping("/{id}/statement")
    @Operation(summary = "Get a derived customer statement from immutable ledger events")
    public ResponseEntity<CustomerStatementResponse> statement(
            @PathVariable Long id,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(required = false) BusinessEventType transactionType) {
        return ResponseEntity.ok(customerLedgerService.getStatement(id, startDate, endDate, transactionType));
    }

    @PostMapping
    public ResponseEntity<CustomerResponse> create(@Valid @RequestBody CustomerRequest req) {
        return ResponseEntity.ok(customerService.create(req));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CustomerResponse> update(@PathVariable Long id, @Valid @RequestBody CustomerRequest req) {
        return ResponseEntity.ok(customerService.update(id, req));
    }
}
