package com.nexusmarket.orders.service;

import com.nexusmarket.adapters.useCases.OrderUseCaseImpl;
import com.nexusmarket.domain.exceptions.BusinessRuleException;
import com.nexusmarket.domain.exceptions.InsufficientStockException;
import com.nexusmarket.domain.exceptions.InvalidStatusTransitionException;
import com.nexusmarket.domain.exceptions.ProductNotActiveException;
import com.nexusmarket.domain.models.Buyer;
import com.nexusmarket.domain.models.Inventory;
import com.nexusmarket.domain.models.Order;
import com.nexusmarket.domain.models.Product;
import com.nexusmarket.domain.models.User;
import com.nexusmarket.domain.ports.in.buyer.OrderUseCasePort;
import com.nexusmarket.domain.ports.out.BuyerRepositoryPort;
import com.nexusmarket.domain.ports.out.InventoryRepositoryPort;
import com.nexusmarket.domain.ports.out.OrderRepositoryPort;
import com.nexusmarket.domain.ports.out.ProductRepositoryPort;
import com.nexusmarket.domain.ports.out.UserRepositoryPort;
import com.nexusmarket.domain.services.OrderCalculationService;
import com.nexusmarket.domain.services.OrderCreateService;
import com.nexusmarket.domain.services.OrderLifecycleService;
import com.nexusmarket.domain.valueobjects.BuyerCommercialStatus;
import com.nexusmarket.domain.valueobjects.InventoryStatus;
import com.nexusmarket.domain.valueobjects.OrderStatus;
import com.nexusmarket.domain.valueobjects.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
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
    @Mock
    private InventoryRepositoryPort inventoryRepositoryPort;

    private OrderUseCasePort orderUseCase;

    private Buyer buyer;
    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder().id(2L).email("buyer@test.com").status(UserStatus.ACTIVE).build();
        buyer = Buyer.builder().id(1L).userId(2L).commercialStatus(BuyerCommercialStatus.ACTIVE).build();

        OrderCreateService createService = new OrderCreateService(orderRepositoryPort, buyerRepositoryPort, userRepositoryPort);
        OrderCalculationService calculationService = new OrderCalculationService(orderRepositoryPort, productRepositoryPort, inventoryRepositoryPort);
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

    private Order cartOrder() {
        Order order = new Order();
        order.setId(10L);
        order.setStatus(OrderStatus.CART);
        return order;
    }

    private Product product(boolean active) {
        return Product.builder()
                .id(5L)
                .sku("SKU-5")
                .name("Product 5")
                .price(BigDecimal.TEN)
                .active(active)
                .build();
    }

    private Inventory inventory(int quantity, InventoryStatus status) {
        return Inventory.builder()
                .productId(5L)
                .quantity(quantity)
                .status(status)
                .build();
    }

    @Test
    void addItem_Success_WhenProductActiveAndStockAvailable() {
        when(orderRepositoryPort.findById(10L)).thenReturn(Optional.of(cartOrder()));
        when(productRepositoryPort.findById(5L)).thenReturn(Optional.of(product(true)));
        when(inventoryRepositoryPort.findByProductId(5L))
                .thenReturn(List.of(inventory(3, InventoryStatus.AVAILABLE)));
        when(orderRepositoryPort.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));

        Order result = orderUseCase.addItem(10L, 5L, 2);

        assertEquals(1, result.getItems().size());
        assertEquals(BigDecimal.valueOf(20.00).compareTo(result.getTotalAmount()), 0);
    }

    @Test
    void addItem_ThrowsException_WhenProductNotFound() {
        when(orderRepositoryPort.findById(10L)).thenReturn(Optional.of(cartOrder()));
        when(productRepositoryPort.findById(99L)).thenReturn(Optional.empty());

        assertThrows(com.nexusmarket.domain.exceptions.ResourceNotFoundException.class,
                () -> orderUseCase.addItem(10L, 99L, 1));
        verify(orderRepositoryPort, never()).save(any(Order.class));
    }

    @Test
    void addItem_ThrowsException_WhenProductIsInactive() {
        when(orderRepositoryPort.findById(10L)).thenReturn(Optional.of(cartOrder()));
        when(productRepositoryPort.findById(5L)).thenReturn(Optional.of(product(false)));

        ProductNotActiveException exception = assertThrows(ProductNotActiveException.class,
                () -> orderUseCase.addItem(10L, 5L, 1));

        assertEquals("Product with id '5' is not active and cannot be added to an order",
                exception.getMessage());
        verify(orderRepositoryPort, never()).save(any(Order.class));
    }

    @Test
    void addItem_ThrowsException_WhenStockIsInsufficient() {
        when(orderRepositoryPort.findById(10L)).thenReturn(Optional.of(cartOrder()));
        when(productRepositoryPort.findById(5L)).thenReturn(Optional.of(product(true)));
        when(inventoryRepositoryPort.findByProductId(5L))
                .thenReturn(List.of(inventory(1, InventoryStatus.AVAILABLE)));

        InsufficientStockException exception = assertThrows(InsufficientStockException.class,
                () -> orderUseCase.addItem(10L, 5L, 5));

        assertTrue(exception.getMessage().contains("Insufficient stock"));
        verify(orderRepositoryPort, never()).save(any(Order.class));
    }

    @Test
    void addItem_ThrowsException_WhenStockIsReservedOrDamaged() {
        when(orderRepositoryPort.findById(10L)).thenReturn(Optional.of(cartOrder()));
        when(productRepositoryPort.findById(5L)).thenReturn(Optional.of(product(true)));
        when(inventoryRepositoryPort.findByProductId(5L)).thenReturn(List.of(
                inventory(10, InventoryStatus.RESERVED),
                inventory(10, InventoryStatus.DAMAGED)));

        assertThrows(InsufficientStockException.class, () -> orderUseCase.addItem(10L, 5L, 1));
    }

    @Test
    void addItem_ThrowsException_WhenNoInventoryRecorded() {
        when(orderRepositoryPort.findById(10L)).thenReturn(Optional.of(cartOrder()));
        when(productRepositoryPort.findById(5L)).thenReturn(Optional.of(product(true)));
        when(inventoryRepositoryPort.findByProductId(5L)).thenReturn(List.of());

        assertThrows(InsufficientStockException.class, () -> orderUseCase.addItem(10L, 5L, 1));
    }
}


