package com.nexusmarket.logistics.service;

import com.nexusmarket.adapters.useCases.ReturnUseCaseImpl;
import com.nexusmarket.domain.exceptions.ReturnAlreadyProcessedException;
import com.nexusmarket.domain.exceptions.ReturnNotAllowedException;
import com.nexusmarket.domain.exceptions.ReturnWindowExpiredException;
import com.nexusmarket.domain.models.Order;
import com.nexusmarket.domain.models.Return;
import com.nexusmarket.domain.ports.in.buyer.ReturnUseCasePort;
import com.nexusmarket.domain.ports.out.OrderRepositoryPort;
import com.nexusmarket.domain.ports.out.ReturnRepositoryPort;
import com.nexusmarket.domain.services.ReturnProcessService;
import com.nexusmarket.domain.valueobjects.OrderStatus;
import com.nexusmarket.domain.valueobjects.ReturnStatus;
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
    private OrderRepositoryPort orderRepositoryPort;

    private ReturnUseCasePort returnUseCase;

    private Order order;

    @BeforeEach
    void setUp() {
        order = new Order();
        order.setId(1L);
        order.setStatus(OrderStatus.DELIVERED);
        order.setOrderDate(LocalDateTime.now().minusDays(5));

        ReturnProcessService returnProcessService = new ReturnProcessService(returnRepositoryPort, orderRepositoryPort);
        returnUseCase = new ReturnUseCaseImpl(returnProcessService, returnRepositoryPort, orderRepositoryPort);
    }

    @Test
    void createReturn_Success() {
        when(orderRepositoryPort.findById(1L)).thenReturn(Optional.of(order));
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

        when(orderRepositoryPort.findById(1L)).thenReturn(Optional.of(order));

        assertThrows(ReturnNotAllowedException.class, () -> returnUseCase.createReturn(1L, "Defective"));
    }

    @Test
    void createReturn_ThrowsReturnWindowExpiredException_WhenOver30Days() {
        order.setOrderDate(LocalDateTime.now().minusDays(35));

        when(orderRepositoryPort.findById(1L)).thenReturn(Optional.of(order));

        assertThrows(ReturnWindowExpiredException.class, () -> returnUseCase.createReturn(1L, "Too late"));
    }

    @Test
    void createReturn_ThrowsReturnAlreadyProcessedException_WhenPendingReturnExists() {
        Return existing = Return.builder().id(2L).status(ReturnStatus.REQUESTED).build();

        when(orderRepositoryPort.findById(1L)).thenReturn(Optional.of(order));
        when(returnRepositoryPort.findByOrderId(1L)).thenReturn(List.of(existing));

        assertThrows(ReturnAlreadyProcessedException.class, () -> returnUseCase.createReturn(1L, "Defective"));
    }

    @Test
    void findAllByBuyerId_aggregatesReturnsAcrossBuyerOrders() {
        Order secondOrder = new Order();
        secondOrder.setId(2L);

        Return firstReturn = Return.builder().id(10L).orderId(1L).build();
        Return secondReturn = Return.builder().id(11L).orderId(2L).build();

        when(orderRepositoryPort.findByBuyerId(20L)).thenReturn(List.of(order, secondOrder));
        when(returnRepositoryPort.findByOrderId(1L)).thenReturn(List.of(firstReturn));
        when(returnRepositoryPort.findByOrderId(2L)).thenReturn(List.of(secondReturn));

        List<Return> result = returnUseCase.findAllByBuyerId(20L);

        assertEquals(List.of(firstReturn, secondReturn), result);
    }

    @Test
    void completeReturn_ThrowsReturnNotAllowedException_WhenNotApproved() {
        Return ret = Return.builder().id(5L).status(ReturnStatus.REQUESTED).build();
        when(returnRepositoryPort.findById(5L)).thenReturn(Optional.of(ret));

        assertThrows(ReturnNotAllowedException.class, () -> returnUseCase.completeReturn(5L));
    }

    @Test
    void approveReturn_ThrowsReturnAlreadyProcessedException_WhenAlreadyRejected() {
        Return ret = Return.builder().id(6L).status(ReturnStatus.REJECTED).build();
        when(returnRepositoryPort.findById(6L)).thenReturn(Optional.of(ret));

        assertThrows(ReturnAlreadyProcessedException.class, () -> returnUseCase.approveReturn(6L));
        verify(returnRepositoryPort, never()).save(any(Return.class));
    }

    @Test
    void rejectReturn_ThrowsReturnAlreadyProcessedException_WhenAlreadyApproved() {
        Return ret = Return.builder().id(7L).status(ReturnStatus.APPROVED).build();
        when(returnRepositoryPort.findById(7L)).thenReturn(Optional.of(ret));

        assertThrows(ReturnAlreadyProcessedException.class, () -> returnUseCase.rejectReturn(7L));
        verify(returnRepositoryPort, never()).save(any(Return.class));
    }

    @Test
    void completeReturn_ThrowsReturnAlreadyProcessedException_WhenRejected() {
        Return ret = Return.builder().id(8L).status(ReturnStatus.REJECTED).build();
        when(returnRepositoryPort.findById(8L)).thenReturn(Optional.of(ret));

        assertThrows(ReturnAlreadyProcessedException.class, () -> returnUseCase.completeReturn(8L));
        verify(returnRepositoryPort, never()).save(any(Return.class));
    }
}


