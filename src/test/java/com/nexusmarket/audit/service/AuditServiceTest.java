package com.nexusmarket.audit.service;

import com.nexusmarket.adapters.useCases.AuditUseCaseImpl;
import com.nexusmarket.domain.exceptions.ResourceNotFoundException;
import com.nexusmarket.domain.models.AuditLog;
import com.nexusmarket.domain.ports.out.AuditLogRepositoryPort;
import com.nexusmarket.domain.services.ConsultAuditLogsService;
import com.nexusmarket.domain.services.RegisterAuditLogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditServiceTest {

    @Mock
    private AuditLogRepositoryPort auditLogRepositoryPort;

    private AuditUseCaseImpl auditUseCase;

    @BeforeEach
    void setUp() {
        RegisterAuditLogService registerAuditLogService = new RegisterAuditLogService(auditLogRepositoryPort);
        ConsultAuditLogsService consultAuditLogsService = new ConsultAuditLogsService(auditLogRepositoryPort);
        auditUseCase = new AuditUseCaseImpl(registerAuditLogService, consultAuditLogsService);
    }

    @Test
    void log_Success() {
        AuditLog saved = AuditLog.builder()
                .id("AUD-1")
                .userId(10L)
                .performedBy("admin@test.com")
                .entityName("Product")
                .entityId("5")
                .action("CREATE")
                .details("Created product")
                .timestamp(LocalDateTime.now())
                .build();

        when(auditLogRepositoryPort.save(any(AuditLog.class))).thenReturn(saved);

        AuditLog response = auditUseCase.log(10L, "admin@test.com", "Product", "5", "CREATE", "Created product");

        assertNotNull(response);
        assertEquals("AUD-1", response.getId());
        assertEquals("CREATE", response.getAction());
    }

    @Test
    void findByUser_Success() {
        AuditLog log = AuditLog.builder().id("1").userId(10L).action("LOGIN").build();
        when(auditLogRepositoryPort.findByUserId(10L)).thenReturn(List.of(log));

        List<AuditLog> logs = auditUseCase.findByUser(10L);

        assertNotNull(logs);
        assertEquals(1, logs.size());
        assertEquals("LOGIN", logs.get(0).getAction());
    }

    @Test
    void getByIdOrThrow_ThrowsResourceNotFoundException_WhenNotFound() {
        when(auditLogRepositoryPort.findById("UNKNOWN")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> auditUseCase.getByIdOrThrow("UNKNOWN"));
    }
}


