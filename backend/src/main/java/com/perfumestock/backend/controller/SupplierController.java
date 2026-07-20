package com.perfumestock.backend.controller;

import com.perfumestock.backend.dto.PageResponse;
import com.perfumestock.backend.entity.Supplier;
import com.perfumestock.backend.service.SupplierService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import java.util.List;

@RestController
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
@RequestMapping("/api/suppliers")
@Tag(name = "Suppliers", description = "Supplier management")
public class SupplierController {
    private final SupplierService service;
    @Autowired public SupplierController(SupplierService service) { this.service = service; }

    @GetMapping
    public ResponseEntity<PageResponse<Supplier>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("name"));
        return ResponseEntity.ok(service.getAll(pageable));
    }

    @GetMapping("/active")
    public ResponseEntity<List<Supplier>> getActive() { return ResponseEntity.ok(service.getActive()); }

    @GetMapping("/{id}")
    public ResponseEntity<Supplier> getById(@PathVariable Long id) { return ResponseEntity.ok(service.getById(id)); }

    @PostMapping
    public ResponseEntity<Supplier> create(@Valid @RequestBody Supplier s) { return ResponseEntity.ok(service.create(s)); }

    @PutMapping("/{id}")
    public ResponseEntity<Supplier> update(@PathVariable Long id, @Valid @RequestBody Supplier s) { return ResponseEntity.ok(service.update(id, s)); }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) { service.delete(id); return ResponseEntity.ok().build(); }
}
