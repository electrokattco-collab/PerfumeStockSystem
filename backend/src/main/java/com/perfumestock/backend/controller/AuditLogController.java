package com.perfumestock.backend.controller;

import com.perfumestock.backend.dto.AuditLogResponse;
import com.perfumestock.backend.dto.PageResponse;
import com.perfumestock.backend.entity.AuditLog;
import com.perfumestock.backend.service.AuditLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@PreAuthorize("hasAnyRole('ADMIN')")
@RequestMapping("/api/audit")
public class AuditLogController {

    private final AuditLogService auditLogService;

    @Autowired
    public AuditLogController(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @GetMapping
    public ResponseEntity<?> getLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        PageResponse<AuditLog> logPage = auditLogService.getLogs(pageable);
        return ResponseEntity.ok(mapPage(logPage));
    }

    @GetMapping("/entity/{entityType}/{entityId}")
    public ResponseEntity<?> getLogsByEntity(
            @PathVariable String entityType,
            @PathVariable Long entityId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        PageResponse<AuditLog> logPage = auditLogService.getLogsByEntity(entityType, entityId, pageable);
        return ResponseEntity.ok(mapPage(logPage));
    }

    private PageResponse<AuditLogResponse> mapPage(PageResponse<AuditLog> page) {
        List<AuditLogResponse> content = page.getContent().stream()
                .map(AuditLogResponse::fromEntity)
                .collect(Collectors.toList());
        return new PageResponse<>(
                content, page.getPage(), page.getSize(), page.getTotalElements(),
                page.getTotalPages(), page.isFirst(), page.isLast(), page.isEmpty()
        );
    }
}
