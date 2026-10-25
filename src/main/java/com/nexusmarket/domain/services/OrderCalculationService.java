package com.nexusmarket.domain.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nexusmarket.domain.exceptions.BusinessRuleException;
import com.nexusmarket.domain.exceptions.ResourceNotFoundException;
import com.nexusmarket.domain.models.Inventory;
import com.nexusmarket.domain.models.Order;
import com.nexusmarket.domain.models.OrderItem;
import com.nexusmarket.domain.models.Product;
import com.nexusmarket.domain.ports.out.InventoryRepositoryPort;
import com.nexusmarket.domain.ports.out.OrderRepositoryPort;
import com.nexusmarket.domain.ports.out.ProductRepositoryPort;
import com.nexusmarket.domain.valueobjects.InventoryStatus;

import lombok.RequiredArgsConstructor;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderCalculationService {

    private final OrderRepositoryPort orderRepositoryPort;
    private final ProductRepositoryPort productRepositoryPort;
    private final InventoryRepositoryPort inventoryRepositoryPort;

    @Transactional
    public Order addItem(Long orderId, Long productId, Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new BusinessRuleException("Item quantity must be greater than zero");
        }

        Order order = orderRepositoryPort.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));

        Product product = productRepositoryPort.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", productId));

        // P2-05: solo se pueden añadir productos activos y con stock disponible.
        if (Boolean.FALSE.equals(product.getActive())) {
            throw new BusinessRuleException(
                    String.format("Product with id '%s' is not active and cannot be added to an order", productId));
        }

        int available = availableStock(productId);
        if (available < quantity) {
            throw new BusinessRuleException(
                    String.format("Insufficient stock for product '%s': requested %d, available %d",
                            productId, quantity, available));
        }

        OrderItem item = OrderItem.builder()
                .productId(product.getId())
                .productName(product.getName())
                .productSku(product.getSku())
                .quantity(quantity)
                .unitPrice(product.getPrice())
                .build();

        order.addItem(item);
        return orderRepositoryPort.save(order);
    }

    @Transactional
    public Order removeItem(Long orderId, Long itemId) {
        Order order = orderRepositoryPort.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));
        order.removeItem(itemId);
        return orderRepositoryPort.save(order);
    }

    /**
     * Stock disponible = suma de inventario AVAILABLE. El inventario RESERVED ya
     * está comprometido por órdenes en curso y DAMAGED no es vendible.
     */
    private int availableStock(Long productId) {
        List<Inventory> inventories = inventoryRepositoryPort.findByProductId(productId);
        if (inventories == null) {
            return 0;
        }
        return inventories.stream()
                .filter(inv -> inv.getStatus() == InventoryStatus.AVAILABLE)
                .map(Inventory::getQuantity)
                .filter(qty -> qty != null)
                .reduce(0, Integer::sum);
    }
}

