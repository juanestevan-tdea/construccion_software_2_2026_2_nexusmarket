package com.nexusmarket.domain.services;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nexusmarket.common.exception.ResourceNotFoundException;
import com.nexusmarket.common.exception.ReturnAlreadyProcessedException;
import com.nexusmarket.common.exception.ReturnNotAllowedException;
import com.nexusmarket.common.exception.ReturnWindowExpiredException;
import com.nexusmarket.domain.models.Order;
import com.nexusmarket.domain.models.Return;
import com.nexusmarket.domain.ports.out.OrderRepositoryPort;
import com.nexusmarket.domain.ports.out.ReturnRepositoryPort;
import com.nexusmarket.domain.valueobjects.OrderStatus;
import com.nexusmarket.domain.valueobjects.ReturnStatus;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReturnProcessService {

    private final ReturnRepositoryPort returnRepositoryPort;
    private final OrderRepositoryPort orderRepositoryPort;

    @Transactional
    public Return createReturn(Long orderId, String reason) {
        Order order = orderRepositoryPort.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));

        if (order.getStatus() != OrderStatus.DELIVERED && order.getStatus() != OrderStatus.FINISHED) {
            throw new ReturnNotAllowedException("Order", "status",
                    "Returns are only allowed for DELIVERED or FINISHED orders. Current: " + order.getStatus());
        }

        if (order.getOrderDate() != null && order.getOrderDate().plusDays(30).isBefore(LocalDateTime.now())) {
            throw new ReturnWindowExpiredException("Order", "orderDate", order.getOrderDate());
        }

        List<Return> existingReturns = returnRepositoryPort.findByOrderId(orderId);
        boolean hasPendingOrApproved = existingReturns.stream()
                .anyMatch(r -> r.getStatus() == ReturnStatus.REQUESTED || r.getStatus() == ReturnStatus.APPROVED);
        if (hasPendingOrApproved) {
            throw new ReturnAlreadyProcessedException("Return", "orderId", order.getId());
        }

        Return returnEntity = Return.builder()
                .orderId(order.getId())
                .reason(reason)
                .status(ReturnStatus.REQUESTED)
                .requestedAt(LocalDateTime.now())
                .build();

        return returnRepositoryPort.save(returnEntity);
    }

    @Transactional
    public Return approveReturn(Long id) {
        Return returnEntity = getByIdOrThrow(id);
        if (returnEntity.getStatus() == ReturnStatus.COMPLETED || returnEntity.getStatus() == ReturnStatus.PROCESSED) {
            throw new ReturnAlreadyProcessedException("Return", "id", id);
        }
        returnEntity.setStatus(ReturnStatus.APPROVED);
        returnEntity.setResolvedAt(LocalDateTime.now());
        return returnRepositoryPort.save(returnEntity);
    }

    @Transactional
    public Return rejectReturn(Long id) {
        Return returnEntity = getByIdOrThrow(id);
        if (returnEntity.getStatus() == ReturnStatus.COMPLETED || returnEntity.getStatus() == ReturnStatus.PROCESSED) {
            throw new ReturnAlreadyProcessedException("Return", "id", id);
        }
        returnEntity.setStatus(ReturnStatus.REJECTED);
        returnEntity.setResolvedAt(LocalDateTime.now());
        return returnRepositoryPort.save(returnEntity);
    }

    @Transactional
    public Return completeReturn(Long id) {
        Return returnEntity = getByIdOrThrow(id);
        if (returnEntity.getStatus() == ReturnStatus.COMPLETED || returnEntity.getStatus() == ReturnStatus.PROCESSED) {
            throw new ReturnAlreadyProcessedException("Return", "id", id);
        }
        if (returnEntity.getStatus() != ReturnStatus.APPROVED) {
            throw new ReturnNotAllowedException("Return", "status", "Return must be approved before completing");
        }
        returnEntity.setStatus(ReturnStatus.COMPLETED);
        returnEntity.setResolvedAt(LocalDateTime.now());
        return returnRepositoryPort.save(returnEntity);
    }

    private Return getByIdOrThrow(Long id) {
        return returnRepositoryPort.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Return", id));
    }
}
