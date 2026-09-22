package com.nexusmarket.logistics.service;

import com.nexusmarket.exception.ResourceNotFoundException;
import com.nexusmarket.exception.ReturnAlreadyProcessedException;
import com.nexusmarket.exception.ReturnNotAllowedException;
import com.nexusmarket.exception.ReturnWindowExpiredException;
import com.nexusmarket.logistics.domain.model.Return;
import com.nexusmarket.logistics.domain.model.ReturnStatus;
import com.nexusmarket.logistics.domain.repository.ReturnRepository;
import com.nexusmarket.logistics.dto.ReturnCreateRequest;
import com.nexusmarket.logistics.dto.ReturnResponse;
import com.nexusmarket.orders.domain.model.Order;
import com.nexusmarket.orders.domain.model.OrderStatus;
import com.nexusmarket.orders.domain.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
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
    private ReturnRepository returnRepository;

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private ReturnService returnService;

    private Order order;

    @BeforeEach
    void setUp() {
        order = new Order();
        order.setId(1L);
        order.setStatus(OrderStatus.DELIVERED);
        order.setOrderDate(LocalDateTime.now().minusDays(5));
    }

    @Test
    void createReturn_Success() {
        ReturnCreateRequest request = ReturnCreateRequest.builder()
                .orderId(1L)
                .reason("Defective product")
                .build();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(returnRepository.findByOrder(order)).thenReturn(Collections.emptyList());
        when(returnRepository.save(any(Return.class))).thenAnswer(i -> {
            Return ret = i.getArgument(0);
            ret.setId(10L);
            return ret;
        });

        ReturnResponse response = returnService.createReturn(request);

        assertNotNull(response);
        assertEquals("Defective product", response.getReason());
        assertEquals(ReturnStatus.REQUESTED, response.getStatus());
    }

    @Test
    void createReturn_ThrowsReturnNotAllowedException_WhenOrderNotDeliveredOrFinished() {
        order.setStatus(OrderStatus.PAID);
        ReturnCreateRequest request = ReturnCreateRequest.builder()
                .orderId(1L)
                .reason("Defective")
                .build();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThrows(ReturnNotAllowedException.class, () -> returnService.createReturn(request));
    }

    @Test
    void createReturn_ThrowsReturnWindowExpiredException_WhenOver30Days() {
        order.setOrderDate(LocalDateTime.now().minusDays(35));
        ReturnCreateRequest request = ReturnCreateRequest.builder()
                .orderId(1L)
                .reason("Too late")
                .build();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThrows(ReturnWindowExpiredException.class, () -> returnService.createReturn(request));
    }

    @Test
    void createReturn_ThrowsReturnAlreadyProcessedException_WhenPendingReturnExists() {
        Return existing = Return.builder().id(2L).status(ReturnStatus.REQUESTED).build();
        ReturnCreateRequest request = ReturnCreateRequest.builder()
                .orderId(1L)
                .reason("Defective")
                .build();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(returnRepository.findByOrder(order)).thenReturn(List.of(existing));

        assertThrows(ReturnAlreadyProcessedException.class, () -> returnService.createReturn(request));
    }

    @Test
    void completeReturn_ThrowsReturnNotAllowedException_WhenNotApproved() {
        Return ret = Return.builder().id(5L).status(ReturnStatus.REQUESTED).build();
        when(returnRepository.findById(5L)).thenReturn(Optional.of(ret));

        assertThrows(ReturnNotAllowedException.class, () -> returnService.completeReturn(5L));
    }
}
