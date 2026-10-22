package com.nexusmarket.auth.mongodb;

import com.nexusmarket.adapters.persistence.mongodb.adapters.SessionAuditMongoAdapter;
import com.nexusmarket.adapters.persistence.mongodb.documents.SessionEventDocument;
import com.nexusmarket.adapters.persistence.mongodb.repositories.SessionEventMongoRepository;
import com.nexusmarket.domain.models.SessionEvent;
import com.nexusmarket.domain.ports.in.model.PageResult;
import com.nexusmarket.domain.ports.in.model.SessionEventQuery;
import com.nexusmarket.domain.valueobjects.SessionEventType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
        SessionEvent data = new SessionEvent(
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

    @Test
    @DisplayName("record propaga el fallo de persistencia para que el servicio decida")
    void record_propagatesPersistenceFailure() {
        SessionEvent data = new SessionEvent(
                "buyer@test.com", SessionEventType.LOGIN, "jti-123", null,
                "192.168.1.100", "Mozilla/5.0", 0, Instant.now());
        when(repository.save(any(SessionEventDocument.class)))
                .thenThrow(new IllegalStateException("MongoDB unavailable"));

        assertThatThrownBy(() -> adapter.record(data))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("MongoDB unavailable");
    }

    @Test
    @DisplayName("find usa la consulta sin filtros y límites seguros cuando no se envían fechas")
    void find_usesSafeDateBoundsWhenDatesAreMissing() {
        when(repository.findByOccurredAtBetween(
                eq(Instant.EPOCH), eq(Instant.parse("9999-12-31T23:59:59Z")),
                any(Pageable.class))).thenReturn(Page.empty());

        PageResult<SessionEvent> result = adapter.find(
                new SessionEventQuery(null, null, null, null, 0, 20));

        assertThat(result.content()).isEmpty();
        verify(repository).findByOccurredAtBetween(
                eq(Instant.EPOCH), eq(Instant.parse("9999-12-31T23:59:59Z")),
                any(Pageable.class));
    }

    @Test
    @DisplayName("find combina email y tipo sin convertir filtros nulos en igualdades")
    void find_delegatesAllFiltersAndMapsPage() {
        Instant from = Instant.parse("2026-01-01T00:00:00Z");
        Instant to = Instant.parse("2026-01-31T23:59:59Z");
        SessionEventQuery query = new SessionEventQuery(
                "buyer@test.com", SessionEventType.LOGIN, from, to, 1, 5);

        SessionEventDocument document = SessionEventDocument.builder()
                .userEmail("buyer@test.com")
                .eventType(SessionEventType.LOGIN)
                .tokenId("jti-1")
                .ipAddress("127.0.0.1")
                .occurredAt(from)
                .build();
        Page<SessionEventDocument> page = new PageImpl<>(
                List.of(document), PageRequest.of(1, 5), 12);
        when(repository.findByUserEmailAndEventTypeAndOccurredAtBetween(
                eq("buyer@test.com"), eq(SessionEventType.LOGIN),
                eq(from), eq(to), any(Pageable.class))).thenReturn(page);

        PageResult<SessionEvent> result = adapter.find(query);

        assertThat(result.content()).hasSize(1);
        assertThat(result.content().get(0).tokenId()).isEqualTo("jti-1");
        assertThat(result.page()).isEqualTo(1);
        assertThat(result.size()).isEqualTo(5);
        assertThat(result.totalElements()).isEqualTo(12);
        assertThat(result.totalPages()).isEqualTo(3);
    }

    @Test
    @DisplayName("find usa la consulta de email cuando no hay tipo")
    void find_usesEmailOnlyQueryWhenTypeIsMissing() {
        Instant from = Instant.parse("2026-01-01T00:00:00Z");
        Instant to = Instant.parse("2026-01-31T23:59:59Z");
        when(repository.findByUserEmailAndOccurredAtBetween(
                eq("buyer@test.com"), eq(from), eq(to), any(Pageable.class)))
                .thenReturn(Page.empty());

        adapter.find(new SessionEventQuery("buyer@test.com", null, from, to, 0, 20));

        verify(repository).findByUserEmailAndOccurredAtBetween(
                eq("buyer@test.com"), eq(from), eq(to), any(Pageable.class));
    }

    @Test
    @DisplayName("find usa la consulta de tipo cuando no hay email")
    void find_usesTypeOnlyQueryWhenEmailIsMissing() {
        Instant from = Instant.parse("2026-01-01T00:00:00Z");
        Instant to = Instant.parse("2026-01-31T23:59:59Z");
        when(repository.findByEventTypeAndOccurredAtBetween(
                eq(SessionEventType.LOGIN), eq(from), eq(to), any(Pageable.class)))
                .thenReturn(Page.empty());

        adapter.find(new SessionEventQuery(null, SessionEventType.LOGIN, from, to, 0, 20));

        verify(repository).findByEventTypeAndOccurredAtBetween(
                eq(SessionEventType.LOGIN), eq(from), eq(to), any(Pageable.class));
    }
}
