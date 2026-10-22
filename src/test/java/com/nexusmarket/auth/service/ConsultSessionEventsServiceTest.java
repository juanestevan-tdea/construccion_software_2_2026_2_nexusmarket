package com.nexusmarket.auth.service;

import com.nexusmarket.domain.models.SessionEvent;
import com.nexusmarket.domain.ports.in.model.PageResult;
import com.nexusmarket.domain.ports.in.model.SessionEventQuery;
import com.nexusmarket.domain.ports.out.SessionAuditPort;
import com.nexusmarket.domain.services.ConsultSessionEventsService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConsultSessionEventsServiceTest {

    @Mock
    private SessionAuditPort sessionAuditPort;

    @Test
    @DisplayName("delega la consulta paginada en SessionAuditPort")
    void delegatesToPort() {
        ConsultSessionEventsService service = new ConsultSessionEventsService(sessionAuditPort);
        SessionEventQuery query = new SessionEventQuery(null, null, null, null, 0, 20);
        PageResult<SessionEvent> expected = new PageResult<>(List.of(), 0, 20, 0, 0);
        when(sessionAuditPort.find(query)).thenReturn(expected);

        assertThat(service.findSessionEvents(query)).isEqualTo(expected);
        verify(sessionAuditPort).find(query);
    }
}
