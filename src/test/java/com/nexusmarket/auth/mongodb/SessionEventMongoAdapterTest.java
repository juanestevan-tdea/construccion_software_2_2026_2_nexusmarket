package com.nexusmarket.auth.mongodb;

import com.nexusmarket.adapters.persistence.mongodb.adapters.SessionAuditMongoAdapter;
import com.nexusmarket.adapters.persistence.mongodb.documents.SessionEventDocument;
import com.nexusmarket.adapters.persistence.mongodb.repositories.SessionEventMongoRepository;
import com.nexusmarket.domain.valueobjects.SessionEventType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test unitario del adaptador Mongo de auditoría de sesiones.
 * Verifica que {@link SessionAuditMongoAdapter} delega correctamente en el
 * repositorio Mongo, sin requerir una instancia de MongoDB real.
 */
@ExtendWith(MockitoExtension.class)
class SessionEventMongoAdapterTest {

    @Mock
    private SessionEventMongoRepository repository;

    private SessionAuditMongoAdapter adapter;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        adapter = new SessionAuditMongoAdapter(repository);
    }

    @Test
    @DisplayName("record delega en repository.save con el documento mapeado")
    void record_delegatesToRepositorySave() {
        com.nexusmarket.domain.ports.out.SessionAuditPort.SessionEventData data =
                new com.nexusmarket.domain.ports.out.SessionAuditPort.SessionEventData(
                        "buyer@test.com", SessionEventType.LOGIN, "jti-123", null,
                        "192.168.1.100", "Mozilla/5.0", 0, Instant.now());

        adapter.record(data);

        ArgumentCaptor<SessionEventDocument> captor =
                ArgumentCaptor.forClass(SessionEventDocument.class);
        verify(repository, times(1)).save(captor.capture());

        SessionEventDocument saved = captor.getValue();
        assertThat(saved.getUserEmail()).isEqualTo("buyer@test.com");
        assertThat(saved.getEventType()).isEqualTo(SessionEventType.LOGIN);
        assertThat(saved.getTokenId()).isEqualTo("jti-123");
        assertThat(saved.getIpAddress()).isEqualTo("192.168.1.100");
        assertThat(saved.getUserAgent()).isEqualTo("Mozilla/5.0");
        assertThat(saved.getTokenVersion()).isEqualTo(0);
        assertThat(saved.getOccurredAt()).isNotNull();
    }
}
