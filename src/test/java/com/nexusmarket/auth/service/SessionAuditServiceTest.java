package com.nexusmarket.auth.service;

import com.nexusmarket.domain.ports.out.SessionAuditPort;
import com.nexusmarket.domain.services.SessionAuditService;
import com.nexusmarket.domain.valueobjects.SessionEventType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SessionAuditServiceTest {

    @Mock
    private SessionAuditPort sessionAuditPort;

    @Mock
    private HttpServletRequest request;

    private SessionAuditService sessionAuditService;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        sessionAuditService = new SessionAuditService(sessionAuditPort);
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("192.168.1.100");
        when(request.getHeader("User-Agent")).thenReturn("Mozilla/5.0 Test");
    }

    @Test
    @DisplayName("recordLogin llama al puerto con tipo LOGIN y datos del request")
    void recordLogin_callsPortWithLoginEvent() {
        String email = "buyer@test.com";
        String jti = UUID.randomUUID().toString();

        sessionAuditService.recordLogin(email, jti, request);

        ArgumentCaptor<SessionAuditPort.SessionEventData> captor =
                ArgumentCaptor.forClass(SessionAuditPort.SessionEventData.class);
        verify(sessionAuditPort, times(1)).record(captor.capture());

        SessionAuditPort.SessionEventData event = captor.getValue();
        assertThat(event.userEmail()).isEqualTo(email);
        assertThat(event.type()).isEqualTo(SessionEventType.LOGIN);
        assertThat(event.tokenId()).isEqualTo(jti);
        assertThat(event.reason()).isNull();
        assertThat(event.ipAddress()).isEqualTo("192.168.1.100");
        assertThat(event.userAgent()).isEqualTo("Mozilla/5.0 Test");
        assertThat(event.tokenVersion()).isNull();
        assertThat(event.occurredAt()).isNotNull();
        assertThat(event.occurredAt()).isBeforeOrEqualTo(Instant.now());
    }

    @Test
    @DisplayName("recordTokenRejected llama al puerto con tipo TOKEN_REJECTED y motivo")
    void recordTokenRejected_callsPortWithRejectedEvent() {
        String email = "buyer@test.com";
        String jti = UUID.randomUUID().toString();
        String reason = "token version obsolete";
        Integer tokenVersion = 0;

        sessionAuditService.recordTokenRejected(email, jti, reason, request, tokenVersion);

        ArgumentCaptor<SessionAuditPort.SessionEventData> captor =
                ArgumentCaptor.forClass(SessionAuditPort.SessionEventData.class);
        verify(sessionAuditPort, times(1)).record(captor.capture());

        SessionAuditPort.SessionEventData event = captor.getValue();
        assertThat(event.userEmail()).isEqualTo(email);
        assertThat(event.type()).isEqualTo(SessionEventType.TOKEN_REJECTED);
        assertThat(event.tokenId()).isEqualTo(jti);
        assertThat(event.reason()).isEqualTo(reason);
        assertThat(event.tokenVersion()).isEqualTo(tokenVersion);
        assertThat(event.ipAddress()).isEqualTo("192.168.1.100");
    }

    @Test
    @DisplayName("recordLoginFailed llama al puerto con tipo LOGIN_FAILED sin jti")
    void recordLoginFailed_callsPortWithLoginFailedEvent() {
        String email = "attacker@test.com";
        String reason = "bad credentials";

        sessionAuditService.recordLoginFailed(email, reason, request);

        ArgumentCaptor<SessionAuditPort.SessionEventData> captor =
                ArgumentCaptor.forClass(SessionAuditPort.SessionEventData.class);
        verify(sessionAuditPort, times(1)).record(captor.capture());

        SessionAuditPort.SessionEventData event = captor.getValue();
        assertThat(event.userEmail()).isEqualTo(email);
        assertThat(event.type()).isEqualTo(SessionEventType.LOGIN_FAILED);
        assertThat(event.tokenId()).isNull();
        assertThat(event.reason()).isEqualTo(reason);
        assertThat(event.tokenVersion()).isNull();
    }

    @Test
    @DisplayName("recordTokenInvalid con email nulo cuando no se puede identificar al usuario")
    void recordTokenInvalid_nullEmail() {
        String jti = UUID.randomUUID().toString();

        sessionAuditService.recordTokenInvalid(jti, "invalid signature", request, null);

        ArgumentCaptor<SessionAuditPort.SessionEventData> captor =
                ArgumentCaptor.forClass(SessionAuditPort.SessionEventData.class);
        verify(sessionAuditPort, times(1)).record(captor.capture());

        SessionAuditPort.SessionEventData event = captor.getValue();
        assertThat(event.userEmail()).isNull();
        assertThat(event.type()).isEqualTo(SessionEventType.TOKEN_INVALID);
        assertThat(event.tokenId()).isEqualTo(jti);
    }

    @Test
    @DisplayName("si el puerto falla, el servicio loguea ERROR y no propaga la excepción")
    void portFailure_doesNotPropagateException() {
        String email = "buyer@test.com";
        doThrow(new RuntimeException("Mongo down"))
                .when(sessionAuditPort).record(any());

        // No debe lanzar excepción
        sessionAuditService.recordLogin(email, "jti-123", request);

        verify(sessionAuditPort, times(1)).record(any());
    }
}
