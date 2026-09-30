package com.nexusmarket.domain.services;

import com.nexusmarket.domain.models.AuditLog;
import com.nexusmarket.domain.ports.out.AuditLogRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RegisterAuditLogService {

    private final AuditLogRepositoryPort auditLogRepositoryPort;

    public AuditLog execute(AuditLog auditLog) {
        if (auditLog == null) {
            throw new IllegalArgumentException("Audit log must not be null");
        }
        return auditLogRepositoryPort.save(auditLog);
    }
}
