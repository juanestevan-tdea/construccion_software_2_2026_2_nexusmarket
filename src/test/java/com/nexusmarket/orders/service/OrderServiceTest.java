package com.nexusmarket.orders.service;

import com.nexusmarket.adapters.useCases.OrderUseCaseImpl;
import com.nexusmarket.domain.exceptions.BusinessRuleException;
import com.nexusmarket.domain.exceptions.InvalidStatusTransitionException;
import com.nexusmarket.domain.models.Buyer;
import com.nexusmarket.domain.models.Order;
import com.nexusmarket.domain.models.User;
import com.nexusmarket.domain.ports.in.OrderUseCasePort;
import com.nexusmarket.domain.ports.out.BuyerRepositoryPort;
import com.nexusmarket.domain.ports.out.OrderRepositoryPort;
import com.nexusmarket.domain.ports.out.ProductRepositoryPort;
import com.nexusmarket.domain.ports.out.UserRepositoryPort;
import com.nexusmarket.domain.services.OrderCalculationService;
import com.nexusmarket.domain.services.OrderCreateService;
import com.nexusmarket.domain.services.OrderLifecycleService;
import com.nexusmarket.domain.valueobjects.BuyerCommercialStatus;
import com.nexusmarket.domain.valueobjects.OrderStatus;
import com.nexusmarket.domain.valueobjects.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepositoryPort orderRepositoryPort;
    @Mock
    private BuyerRepositoryPort buyerRepositoryPort;
    @Mock
    private UserRepositoryPort userRepositoryPort;
    @Mock
    private ProductRepositoryPort productRepositoryPort;

    private OrderUseCasePort orderUseCase;

    private Buyer buyer;
    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder().id(2L).email("buyer@test.com").status(UserStatus.ACTIVE).build();
        buyer = Buyer.builder().id(1L).userId(2L).commercialStatus(BuyerCommercialStatus.ACTIVE).build();

        OrderCreateService createService = new OrderCreateService(orderRepositoryPort, buyerRepositoryPort, userRepositoryPort);
        OrderCalculationService calculationService = new OrderCalculationService(orderRepositoryPort, productRepositoryPort);
        OrderLifecycleService lifecycleService = new OrderLifecycleService(orderRepositoryPort);
        orderUseCase = new OrderUseCaseImpl(createService, calculationService, lifecycleService, orderRepositoryPort, buyerRepositoryPort);
    }

    @Test
    void createOrder_Success() {
        when(buyerRepositoryPort.findById(1L)).thenReturn(Optional.of(buyer));
        when(userRepositoryPort.findById(2L)).thenReturn(Optional.of(user));
        when(orderRepositoryPort.save(any(Order.class))).thenAnswer(i -> {
            Order o = i.getArgument(0);
            o.setId(10L);
            return o;
        });

        Order created = orderUseCase.createOrder(1L);

        assertNotNull(created);
        assertEquals(OrderStatus.CART, created.getStatus());
        assertEquals(1L, created.getBuyerId());
    }

    @Test
    void createOrder_ThrowsException_WhenBuyerNotActive() {
        buyer.setCommercialStatus(BuyerCommercialStatus.INACTIVE);
        when(buyerRepositoryPort.findById(1L)).thenReturn(Optional.of(buyer));

        assertThrows(BusinessRuleException.class, () -> orderUseCase.createOrder(1L));
    }

    @Test
    void confirmPayment_ThrowsException_WhenNotPendingPayment() {
        Order order = new Order();
        order.setId(10L);
        order.setStatus(OrderStatus.CART);
        when(orderRepositoryPort.findById(10L)).thenReturn(Optional.of(order));

        assertThrows(InvalidStatusTransitionException.class, () -> orderUseCase.confirmPayment(10L));
    }

    @Test
    void cancel_ThrowsException_WhenOrderIsFinished() {
        Order order = new Order();
        order.setId(10L);
        order.setStatus(OrderStatus.FINISHED);
        when(orderRepositoryPort.findById(10L)).thenReturn(Optional.of(order));

        assertThrows(InvalidStatusTransitionException.class, () -> orderUseCase.cancel(10L));
    }
}


