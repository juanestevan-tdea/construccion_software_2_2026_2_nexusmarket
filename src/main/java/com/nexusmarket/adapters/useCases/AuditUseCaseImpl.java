package com.nexusmarket.adapters.useCases;

import com.nexusmarket.domain.models.AuditLog;
import com.nexusmarket.domain.ports.in.AuditUseCasePort;
import com.nexusmarket.domain.services.ConsultAuditLogsService;
import com.nexusmarket.domain.services.RegisterAuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditUseCaseImpl implements AuditUseCasePort {

    private final RegisterAuditLogService registerAuditLogService;
    private final ConsultAuditLogsService consultAuditLogsService;

    @Override
    public AuditLog log(Long userId, String performedBy, String entityName, String entityId, String action, String details) {
        AuditLog auditLog = AuditLog.builder()
                .userId(userId)
                .performedBy(performedBy)
                .entityName(entityName)
                .entityId(entityId)
                .action(action)
                .details(details)
                .timestamp(LocalDateTime.now())
                .build();
        return registerAuditLogService.execute(auditLog);
    }

    @Override
    public List<AuditLog> findAll() {
        return consultAuditLogsService.findAll();
    }

    @Override
    public List<AuditLog> findByUser(Long userId) {
        return consultAuditLogsService.findByUser(userId);
    }

    @Override
    public List<AuditLog> findByEntity(String entityName) {
        return consultAuditLogsService.findByEntity(entityName);
    }

    @Override
    public List<AuditLog> findByDateRange(LocalDateTime start, LocalDateTime end) {
        return consultAuditLogsService.findByDateRange(start, end);
    }

    @Override
    public AuditLog getByIdOrThrow(String id) {
        return consultAuditLogsService.getByIdOrThrow(id);
    }
}
