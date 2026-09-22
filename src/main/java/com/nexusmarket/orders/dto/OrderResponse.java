package com.nexusmarket.orders.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import com.nexusmarket.orders.domain.model.Order;
import com.nexusmarket.orders.domain.model.OrderStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {

    private Long id;
    private Long buyerId;
    private String buyerEmail;
    private LocalDateTime orderDate;
    private OrderStatus status;
    private BigDecimal totalAmount;
    private List<OrderItemResponse> items;

    public static OrderResponse fromEntity(Order order) {
        if (order == null) {
            return null;
        }
        List<OrderItemResponse> itemList = order.getItems() != null
                ? order.getItems().stream().map(OrderItemResponse::fromEntity).toList()
                : Collections.emptyList();

        return OrderResponse.builder()
                .id(order.getId())
                .buyerId(order.getBuyer() != null ? order.getBuyer().getId() : null)
                .buyerEmail(order.getBuyer() != null && order.getBuyer().getUser() != null
                        ? order.getBuyer().getUser().getEmail() : null)
                .orderDate(order.getOrderDate())
                .status(order.getStatus())
                .totalAmount(order.getTotalAmount())
                .items(itemList)
                .build();
    }
}
