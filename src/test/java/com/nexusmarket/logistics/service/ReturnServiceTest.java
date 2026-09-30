package com.nexusmarket.logistics.service;

import com.nexusmarket.adapters.useCases.ReturnUseCaseImpl;
import com.nexusmarket.common.exception.ReturnAlreadyProcessedException;
import com.nexusmarket.common.exception.ReturnNotAllowedException;
import com.nexusmarket.common.exception.ReturnWindowExpiredException;
import com.nexusmarket.domain.models.Return;
import com.nexusmarket.domain.ports.in.ReturnUseCasePort;
import com.nexusmarket.domain.ports.out.ReturnRepositoryPort;
import com.nexusmarket.domain.services.ReturnProcessService;
import com.nexusmarket.domain.valueobjects.ReturnStatus;
import com.nexusmarket.orders.domain.model.Order;
import com.nexusmarket.orders.domain.model.OrderStatus;
import com.nexusmarket.orders.domain.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReturnServiceTest {

    @Mock
    private ReturnRepositoryPort returnRepositoryPort;

    @Mock
    private OrderRepository orderRepository;

    private ReturnUseCasePort returnUseCase;

    private Order order;

    @BeforeEach
    void setUp() {
        order = new Order();
        order.setId(1L);
        order.setStatus(OrderStatus.DELIVERED);
        order.setOrderDate(LocalDateTime.now().minusDays(5));

        ReturnProcessService returnProcessService = new ReturnProcessService(returnRepositoryPort, orderRepository);
        returnUseCase = new ReturnUseCaseImpl(returnProcessService, returnRepositoryPort, orderRepository);
    }

    @Test
    void createReturn_Success() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(returnRepositoryPort.findByOrderId(1L)).thenReturn(Collections.emptyList());
        when(returnRepositoryPort.save(any(Return.class))).thenAnswer(i -> {
            Return ret = i.getArgument(0);
            ret.setId(10L);
            return ret;
        });

        Return response = returnUseCase.createReturn(1L, "Defective product");

        assertNotNull(response);
        assertEquals("Defective product", response.getReason());
        assertEquals(ReturnStatus.REQUESTED, response.getStatus());
    }

    @Test
    void createReturn_ThrowsReturnNotAllowedException_WhenOrderNotDeliveredOrFinished() {
        order.setStatus(OrderStatus.PAID);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThrows(ReturnNotAllowedException.class, () -> returnUseCase.createReturn(1L, "Defective"));
    }

    @Test
    void createReturn_ThrowsReturnWindowExpiredException_WhenOver30Days() {
        order.setOrderDate(LocalDateTime.now().minusDays(35));

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThrows(ReturnWindowExpiredException.class, () -> returnUseCase.createReturn(1L, "Too late"));
    }

    @Test
    void createReturn_ThrowsReturnAlreadyProcessedException_WhenPendingReturnExists() {
        Return existing = Return.builder().id(2L).status(ReturnStatus.REQUESTED).build();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(returnRepositoryPort.findByOrderId(1L)).thenReturn(List.of(existing));

        assertThrows(ReturnAlreadyProcessedException.class, () -> returnUseCase.createReturn(1L, "Defective"));
    }

    @Test
    void completeReturn_ThrowsReturnNotAllowedException_WhenNotApproved() {
        Return ret = Return.builder().id(5L).status(ReturnStatus.REQUESTED).build();
        when(returnRepositoryPort.findById(5L)).thenReturn(Optional.of(ret));

        assertThrows(ReturnNotAllowedException.class, () -> returnUseCase.completeReturn(5L));
    }
}

