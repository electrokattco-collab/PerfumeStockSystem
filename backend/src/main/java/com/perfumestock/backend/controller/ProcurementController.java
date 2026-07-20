package com.perfumestock.backend.controller;

import com.perfumestock.backend.dto.PageResponse;
import com.perfumestock.backend.dto.ProcurementRequest;
import com.perfumestock.backend.dto.ProcurementResponse;
import com.perfumestock.backend.entity.Procurement;
import com.perfumestock.backend.service.ProcurementService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/procurements")
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
public class ProcurementController {

    private final ProcurementService procurementService;

    @Autowired
    public ProcurementController(ProcurementService procurementService) {
        this.procurementService = procurementService;
    }

    private String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : "system";
    }

    @GetMapping
    public ResponseEntity<?> getAllProcurements(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "desc") String direction) {

        Sort.Direction sortDir = "desc".equalsIgnoreCase(direction) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDir, sort));

        PageResponse<Procurement> procurementPage = procurementService.getAllProcurements(pageable);
        PageResponse<ProcurementResponse> response = mapPage(procurementPage);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProcurementResponse> getProcurementById(@PathVariable Long id) {
        Procurement procurement = procurementService.getProcurementById(id);
        return ResponseEntity.ok(ProcurementResponse.fromEntity(procurement));
    }

    @PostMapping
    public ResponseEntity<ProcurementResponse> createProcurement(
            @Valid @RequestBody ProcurementRequest request) {
        Procurement procurement = procurementService.createProcurement(request, getCurrentUsername());
        return ResponseEntity.ok(ProcurementResponse.fromEntity(procurement));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProcurementResponse> updateProcurement(
            @PathVariable Long id,
            @Valid @RequestBody ProcurementRequest request) {
        Procurement procurement = procurementService.updateProcurement(id, request, getCurrentUsername());
        return ResponseEntity.ok(ProcurementResponse.fromEntity(procurement));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProcurement(@PathVariable Long id) {
        procurementService.deleteProcurement(id, getCurrentUsername());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/confirm")
    public ResponseEntity<ProcurementResponse> confirmProcurement(@PathVariable Long id) {
        Procurement procurement = procurementService.confirmProcurement(id, getCurrentUsername());
        return ResponseEntity.ok(ProcurementResponse.fromEntity(procurement));
    }

    @PostMapping("/{id}/ocr")
    public ResponseEntity<ProcurementResponse> importOcrData(
            @PathVariable Long id,
            @Valid @RequestBody ProcurementRequest request) {
        Procurement procurement = procurementService.importOcrData(request, getCurrentUsername());
        return ResponseEntity.ok(ProcurementResponse.fromEntity(procurement));
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchProcurements(
            @RequestParam(required = false) String supplierName,
            @RequestParam(required = false) String invoiceNumber,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "desc") String direction) {

        Sort.Direction sortDir = "desc".equalsIgnoreCase(direction) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDir, sort));

        PageResponse<Procurement> procurementPage = procurementService.searchProcurements(
                supplierName, invoiceNumber, status, pageable);
        PageResponse<ProcurementResponse> response = mapPage(procurementPage);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboard() {
        return ResponseEntity.ok(procurementService.getDashboardStats());
    }

    private PageResponse<ProcurementResponse> mapPage(PageResponse<Procurement> page) {
        var content = page.getContent().stream()
                .map(ProcurementResponse::fromEntity)
                .collect(Collectors.toList());
        return new PageResponse<>(
                content, page.getPage(), page.getSize(), page.getTotalElements(),
                page.getTotalPages(), page.isFirst(), page.isLast(), page.isEmpty()
        );
    }
}
