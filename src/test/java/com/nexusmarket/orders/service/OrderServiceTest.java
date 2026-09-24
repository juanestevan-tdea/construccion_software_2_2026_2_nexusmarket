package com.nexusmarket.orders.service;

import com.nexusmarket.catalog.domain.repository.ProductRepository;
import com.nexusmarket.common.exception.BusinessRuleException;
import com.nexusmarket.common.exception.InvalidStatusTransitionException;
import com.nexusmarket.orders.domain.model.Order;
import com.nexusmarket.orders.domain.model.OrderStatus;
import com.nexusmarket.orders.domain.repository.OrderRepository;
import com.nexusmarket.users.domain.model.Buyer;
import com.nexusmarket.users.domain.model.BuyerCommercialStatus;
import com.nexusmarket.users.domain.model.User;
import com.nexusmarket.users.domain.repository.BuyerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private BuyerRepository buyerRepository;
    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private OrderService orderService;

    private Buyer buyer;

    @BeforeEach
    void setUp() {
        User user = User.builder().id(2L).email("buyer@test.com").status(com.nexusmarket.users.domain.model.UserStatus.ACTIVE).build();
        buyer = Buyer.builder().id(1L).user(user).commercialStatus(BuyerCommercialStatus.ACTIVE).build();
    }

    @Test
    void createOrder_Success() {
        when(buyerRepository.findById(1L)).thenReturn(Optional.of(buyer));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> {
            Order o = i.getArgument(0);
            o.setId(10L);
            return o;
        });

        Order created = orderService.createOrder(1L);

        assertNotNull(created);
        assertEquals(OrderStatus.CART, created.getStatus());
        assertEquals(buyer, created.getBuyer());
    }

    @Test
    void createOrder_ThrowsException_WhenBuyerNotActive() {
        buyer.setCommercialStatus(BuyerCommercialStatus.INACTIVE);
        when(buyerRepository.findById(1L)).thenReturn(Optional.of(buyer));

        assertThrows(BusinessRuleException.class, () -> orderService.createOrder(1L));
    }

    @Test
    void confirmPayment_ThrowsException_WhenNotPendingPayment() {
        Order order = new Order();
        order.setId(10L);
        order.setStatus(OrderStatus.CART);
        when(orderRepository.findById(10L)).thenReturn(Optional.of(order));

        assertThrows(InvalidStatusTransitionException.class, () -> orderService.confirmPayment(10L));
    }

    @Test
    void cancel_ThrowsException_WhenOrderIsFinished() {
        Order order = new Order();
        order.setId(10L);
        order.setStatus(OrderStatus.FINISHED);
        when(orderRepository.findById(10L)).thenReturn(Optional.of(order));

        assertThrows(InvalidStatusTransitionException.class, () -> orderService.cancel(10L));
    }
}
