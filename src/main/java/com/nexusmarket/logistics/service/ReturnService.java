package com.nexusmarket.logistics.service;

import com.nexusmarket.exception.*;
import com.nexusmarket.logistics.domain.model.Return;
import com.nexusmarket.logistics.domain.model.ReturnStatus;
import com.nexusmarket.logistics.domain.repository.ReturnRepository;
import com.nexusmarket.logistics.dto.ReturnCreateRequest;
import com.nexusmarket.logistics.dto.ReturnResponse;
import com.nexusmarket.orders.domain.model.Order;
import com.nexusmarket.orders.domain.model.OrderStatus;
import com.nexusmarket.orders.domain.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReturnService {

    private final ReturnRepository returnRepository;
    private final OrderRepository orderRepository;

    @Transactional
    public ReturnResponse createReturn(ReturnCreateRequest request) {
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order", request.getOrderId()));

        // Solo devolver órdenes en DELIVERED o FINISHED
        if (order.getStatus() != OrderStatus.DELIVERED && order.getStatus() != OrderStatus.FINISHED) {
            throw new ReturnNotAllowedException("Order", "status",
                    "Returns are only allowed for DELIVERED or FINISHED orders. Current: " + order.getStatus());
        }

        // Ventana de devolución no expirada (30 días desde la fecha de orden/entrega)
        if (order.getOrderDate() != null && order.getOrderDate().plusDays(30).isBefore(LocalDateTime.now())) {
            throw new ReturnWindowExpiredException("Order", "orderDate", order.getOrderDate());
        }

        // No permitir duplicar retorno pendiente
        List<Return> existingReturns = returnRepository.findByOrder(order);
        boolean hasPendingOrApproved = existingReturns.stream()
                .anyMatch(r -> r.getStatus() == ReturnStatus.REQUESTED || r.getStatus() == ReturnStatus.APPROVED);
        if (hasPendingOrApproved) {
            throw new ReturnAlreadyProcessedException("Return", "orderId", order.getId());
        }

        Return returnEntity = Return.builder()
                .order(order)
                .reason(request.getReason())
                .status(ReturnStatus.REQUESTED)
                .requestedAt(LocalDateTime.now())
                .build();

        return ReturnResponse.fromEntity(returnRepository.save(returnEntity));
    }

    @Transactional(readOnly = true)
    public Return getByIdOrThrow(Long id) {
        return returnRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Return", id));
    }

    @Transactional(readOnly = true)
    public ReturnResponse getReturnResponseById(Long id) {
        return ReturnResponse.fromEntity(getByIdOrThrow(id));
    }

    @Transactional(readOnly = true)
    public List<ReturnResponse> findByOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));
        return returnRepository.findByOrder(order).stream()
                .map(ReturnResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReturnResponse> findAll() {
        return returnRepository.findAll().stream()
                .map(ReturnResponse::fromEntity)
                .toList();
    }

    @Transactional
    public ReturnResponse approveReturn(Long id) {
        Return returnEntity = getByIdOrThrow(id);
        if (returnEntity.getStatus() == ReturnStatus.COMPLETED || returnEntity.getStatus() == ReturnStatus.PROCESSED) {
            throw new ReturnAlreadyProcessedException("Return", "id", id);
        }
        returnEntity.setStatus(ReturnStatus.APPROVED);
        returnEntity.setResolvedAt(LocalDateTime.now());
        return ReturnResponse.fromEntity(returnRepository.save(returnEntity));
    }

    @Transactional
    public ReturnResponse rejectReturn(Long id) {
        Return returnEntity = getByIdOrThrow(id);
        if (returnEntity.getStatus() == ReturnStatus.COMPLETED || returnEntity.getStatus() == ReturnStatus.PROCESSED) {
            throw new ReturnAlreadyProcessedException("Return", "id", id);
        }
        returnEntity.setStatus(ReturnStatus.REJECTED);
        returnEntity.setResolvedAt(LocalDateTime.now());
        return ReturnResponse.fromEntity(returnRepository.save(returnEntity));
    }

    @Transactional
    public ReturnResponse completeReturn(Long id) {
        Return returnEntity = getByIdOrThrow(id);
        if (returnEntity.getStatus() == ReturnStatus.COMPLETED || returnEntity.getStatus() == ReturnStatus.PROCESSED) {
            throw new ReturnAlreadyProcessedException("Return", "id", id);
        }
        if (returnEntity.getStatus() != ReturnStatus.APPROVED) {
            throw new ReturnNotAllowedException("Return", "status", "Return must be approved before completing");
        }
        returnEntity.setStatus(ReturnStatus.COMPLETED);
        returnEntity.setResolvedAt(LocalDateTime.now());
        return ReturnResponse.fromEntity(returnRepository.save(returnEntity));
    }
}
