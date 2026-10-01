package com.nexusmarket.adapters.rest.mappers;

import com.nexusmarket.adapters.rest.dtos.responses.OrderItemResponseDTO;
import com.nexusmarket.adapters.rest.dtos.responses.OrderResponseDTO;
import com.nexusmarket.domain.models.Order;
import com.nexusmarket.domain.models.OrderItem;
import lombok.experimental.UtilityClass;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

@UtilityClass
public class OrderRestMapper {

    public OrderResponseDTO toResponseDTO(Order domain) {
        if (domain == null) {
            return null;
        }
        List<OrderItemResponseDTO> items = domain.getItems() != null
                ? domain.getItems().stream().map(OrderRestMapper::toItemResponseDTO).toList()
                : Collections.emptyList();

        return OrderResponseDTO.builder()
                .id(domain.getId())
                .buyerId(domain.getBuyerId())
                .buyerEmail(domain.getBuyerEmail())
                .orderDate(domain.getOrderDate())
                .status(domain.getStatus())
                .totalAmount(domain.getTotalAmount())
                .items(items)
                .build();
    }

    public OrderItemResponseDTO toItemResponseDTO(OrderItem item) {
        if (item == null) {
            return null;
        }
        BigDecimal subtotal = item.getUnitPrice() != null && item.getQuantity() != null
                ? item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()))
                : BigDecimal.ZERO;

        return OrderItemResponseDTO.builder()
                .id(item.getId())
                .productId(item.getProductId())
                .productName(item.getProductName())
                .productSku(item.getProductSku())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .subtotal(subtotal)
                .build();
    }
}
