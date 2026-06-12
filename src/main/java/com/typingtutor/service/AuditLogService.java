package com.typingtutor.service;

import com.typingtutor.entity.AuditLog;
import com.typingtutor.repository.AuditLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class AuditLogService {

    private static final Logger log = LoggerFactory.getLogger(AuditLogService.class);

    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public void log(String username, String action, String details) {
        try {
            auditLogRepository.save(new AuditLog(username, action, details));
        } catch (Exception e) {
            log.warn("Audit log write failed for action={}: {}", action, e.getMessage());
        }
    }
}
