package com.perfumestock.backend.service;

import com.perfumestock.backend.dto.PageResponse;
import com.perfumestock.backend.entity.AuditLog;
import com.perfumestock.backend.repository.AuditLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditLogService {

    private static final Logger log = LoggerFactory.getLogger(AuditLogService.class);

    private final AuditLogRepository auditLogRepository;

    @Autowired
    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional
    public AuditLog log(String entityType, Long entityId, String action,
                        String details, Long userId, String username) {
        AuditLog auditLog = new AuditLog(entityType, entityId, action, details, userId, username);
        AuditLog saved = auditLogRepository.save(auditLog);
        log.debug("Audit: {} {} {} by {}", entityType, action, entityId, username);
        return saved;
    }

    public PageResponse<AuditLog> getLogs(Pageable pageable) {
        Page<AuditLog> page = auditLogRepository.findAll(pageable);
        return PageResponse.of(page);
    }

    public PageResponse<AuditLog> getLogsByEntity(String entityType, Long entityId, Pageable pageable) {
        Page<AuditLog> page = auditLogRepository.findByEntityTypeAndEntityId(entityType, entityId, pageable);
        return PageResponse.of(page);
    }

    public PageResponse<AuditLog> getLogsByUser(Long userId, Pageable pageable) {
        Page<AuditLog> page = auditLogRepository.findByUserId(userId, pageable);
        return PageResponse.of(page);
    }
}
