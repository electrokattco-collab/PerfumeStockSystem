package com.perfumestock.backend.controller;

import com.perfumestock.backend.dto.CustomerBalanceResponse;
import com.perfumestock.backend.dto.CustomerLedgerEntry;
import com.perfumestock.backend.dto.CustomerRequest;
import com.perfumestock.backend.dto.CustomerResponse;
import com.perfumestock.backend.dto.CustomerStatementResponse;
import com.perfumestock.backend.dto.SimpleCustomerResponse;
import com.perfumestock.backend.entity.BusinessEventType;
import com.perfumestock.backend.entity.Customer;
import com.perfumestock.backend.repository.CustomerRepository;
import com.perfumestock.backend.service.CustomerLedgerService;
import com.perfumestock.backend.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerService customerService;
    private final CustomerLedgerService customerLedgerService;
    private final CustomerRepository customerRepository;

    public CustomerController(CustomerRepository customerRepository) {
        this.customerService = null;
        this.customerLedgerService = null;
        this.customerRepository = customerRepository;
    }

    public CustomerController(CustomerService customerService, CustomerLedgerService customerLedgerService) {
        this.customerService = customerService;
        this.customerLedgerService = customerLedgerService;
        this.customerRepository = null;
    }

    @Autowired
    public CustomerController(CustomerRepository customerRepository,
                              CustomerService customerService,
                              CustomerLedgerService customerLedgerService) {
        this.customerService = customerService;
        this.customerLedgerService = customerLedgerService;
        this.customerRepository = customerRepository;
    }

    @GetMapping(params = {"page", "size"})
    public ResponseEntity<Page<CustomerResponse>> listLegacy(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return ResponseEntity.ok(customerService.list(PageRequest.of(page, size)));
    }

    @GetMapping
    public ResponseEntity<List<SimpleCustomerResponse>> list() {
        if (customerRepository == null) {
            return ResponseEntity.ok(List.of());
        }
        return ResponseEntity.ok(customerRepository.findAll()
                .stream()
                .map(SimpleCustomerResponse::from)
                .collect(Collectors.toList()));
    }

    @GetMapping("/search")
    public ResponseEntity<List<SimpleCustomerResponse>> search(@RequestParam String q) {
        if (customerRepository == null) {
            return ResponseEntity.ok(List.of());
        }
        String query = q == null ? "" : q.toLowerCase();
        List<SimpleCustomerResponse> results = customerRepository.findAll()
                .stream()
                .filter(customer -> customer.getName() != null && customer.getName().toLowerCase().contains(query))
                .map(SimpleCustomerResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(results);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SimpleCustomerResponse> getById(@PathVariable Long id) {
        if (customerRepository == null) {
            return ResponseEntity.notFound().build();
        }
        return customerRepository.findById(id)
                .map(SimpleCustomerResponse::from)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/debtors")
    public ResponseEntity<List<SimpleCustomerResponse>> debtors() {
        if (customerRepository == null) {
            return ResponseEntity.ok(List.of());
        }
        List<SimpleCustomerResponse> debtors = customerRepository.findAll()
                .stream()
                .filter(customer -> customer.getAmountOwing() != null && customer.getAmountOwing().compareTo(BigDecimal.ZERO) > 0)
                .map(SimpleCustomerResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(debtors);
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
    public ResponseEntity<SimpleCustomerResponse> create(@Valid @RequestBody CustomerRequest req) {
        if (customerRepository == null) {
            return ResponseEntity.badRequest().build();
        }
        Customer c = new Customer();
        c.setName(req.getName());
        c.setPhone(req.getPhone());
        c.setAddress(req.getAddress());
        c.setNotes(req.getNotes());
        c.setAmountOwing(req.getAmountOwing() != null ? req.getAmountOwing() : BigDecimal.ZERO);
        Customer saved = customerRepository.save(c);
        SimpleCustomerResponse resp = SimpleCustomerResponse.from(saved);
        return ResponseEntity.created(URI.create("/api/customers/" + saved.getId())).body(resp);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SimpleCustomerResponse> update(@PathVariable Long id, @Valid @RequestBody CustomerRequest req) {
        if (customerRepository == null) {
            return ResponseEntity.notFound().build();
        }
        return customerRepository.findById(id).map(existing -> {
            existing.setName(req.getName());
            existing.setPhone(req.getPhone());
            existing.setAddress(req.getAddress());
            existing.setNotes(req.getNotes());
            if (req.getAmountOwing() != null) {
                existing.setAmountOwing(req.getAmountOwing());
            }
            Customer saved = customerRepository.save(existing);
            return ResponseEntity.ok(SimpleCustomerResponse.from(saved));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (customerRepository == null || !customerRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        customerRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
