package com.nexusmarket.adapters.useCases;

import com.nexusmarket.domain.models.SessionEvent;
import com.nexusmarket.domain.ports.in.model.PageResult;
import com.nexusmarket.domain.ports.in.model.SessionEventQuery;
import com.nexusmarket.domain.services.ConsultSessionEventsService;
import com.nexusmarket.domain.valueobjects.SessionEventType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SessionEventQueryUseCaseImplTest {

    @Mock
    private ConsultSessionEventsService consultSessionEventsService;

    @Test
    @DisplayName("normaliza página negativa y tamaño cero antes de delegar")
    void normalizesPagination() {
        SessionEventQueryUseCaseImpl useCase = new SessionEventQueryUseCaseImpl(consultSessionEventsService);
        PageResult<SessionEvent> expected = new PageResult<>(List.of(), 0, 20, 0, 0);
        when(consultSessionEventsService.findSessionEvents(any())).thenReturn(expected);

        PageResult<SessionEvent> result = useCase.findSessionEvents(
                new SessionEventQuery("a@b.com", SessionEventType.LOGIN,
                        Instant.EPOCH, Instant.now(), -5, 0));

        ArgumentCaptor<SessionEventQuery> captor = ArgumentCaptor.forClass(SessionEventQuery.class);
        verify(consultSessionEventsService).findSessionEvents(captor.capture());
        assertThat(captor.getValue().page()).isZero();
        assertThat(captor.getValue().size()).isEqualTo(20);
        assertThat(result).isEqualTo(expected);
    }

    @Test
    @DisplayName("limita el tamaño de página a 100")
    void capsPageSize() {
        SessionEventQueryUseCaseImpl useCase = new SessionEventQueryUseCaseImpl(consultSessionEventsService);
        when(consultSessionEventsService.findSessionEvents(any()))
                .thenReturn(new PageResult<>(List.of(), 0, 100, 0, 0));

        useCase.findSessionEvents(new SessionEventQuery(
                null, null, null, null, 2, 500));

        ArgumentCaptor<SessionEventQuery> captor = ArgumentCaptor.forClass(SessionEventQuery.class);
        verify(consultSessionEventsService).findSessionEvents(captor.capture());
        assertThat(captor.getValue().page()).isEqualTo(2);
        assertThat(captor.getValue().size()).isEqualTo(100);
    }

    @Test
    @DisplayName("normaliza email vacío o con espacios para que no se use como filtro")
    void normalizesBlankEmail() {
        assertThat(new SessionEventQuery("   ", null, null, null, 0, 20).email()).isNull();
        assertThat(new SessionEventQuery(" buyer@test.com ", null, null, null, 0, 20).email())
                .isEqualTo("buyer@test.com");
    }
}
