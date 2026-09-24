package com.nexusmarket.audit.service;

import com.nexusmarket.audit.domain.model.AuditLog;
import com.nexusmarket.audit.domain.repository.AuditLogRepository;
import com.nexusmarket.audit.dto.AuditLogResponse;
import com.nexusmarket.common.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
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
    private AuditLogRepository auditLogRepository;

    @InjectMocks
    private AuditService auditService;

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

        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(saved);

        AuditLogResponse response = auditService.log(10L, "admin@test.com", "Product", "5", "CREATE", "Created product");

        assertNotNull(response);
        assertEquals("AUD-1", response.getId());
        assertEquals("CREATE", response.getAction());
    }

    @Test
    void findByUser_Success() {
        AuditLog log = AuditLog.builder().id("1").userId(10L).action("LOGIN").build();
        when(auditLogRepository.findByUserId(10L)).thenReturn(List.of(log));

        List<AuditLogResponse> logs = auditService.findByUser(10L);

        assertNotNull(logs);
        assertEquals(1, logs.size());
        assertEquals("LOGIN", logs.get(0).getAction());
    }

    @Test
    void getByIdOrThrow_ThrowsResourceNotFoundException_WhenNotFound() {
        when(auditLogRepository.findById("UNKNOWN")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> auditService.getByIdOrThrow("UNKNOWN"));
    }
}
