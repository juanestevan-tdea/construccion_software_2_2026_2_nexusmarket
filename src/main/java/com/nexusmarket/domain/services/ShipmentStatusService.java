package com.nexusmarket.domain.services;

import com.nexusmarket.common.exception.InvalidStatusTransitionException;
import com.nexusmarket.common.exception.ResourceNotFoundException;
import com.nexusmarket.domain.models.Shipment;
import com.nexusmarket.domain.ports.out.ShipmentRepositoryPort;
import com.nexusmarket.domain.valueobjects.ShipmentStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ShipmentStatusService {

    private final ShipmentRepositoryPort shipmentRepositoryPort;

    @Transactional
    public Shipment updateStatus(Long id, ShipmentStatus newStatus) {
        Shipment shipment = shipmentRepositoryPort.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Shipment", id));

        if (shipment.getStatus() == ShipmentStatus.DELIVERED && newStatus == ShipmentStatus.CANCELLED) {
            throw new InvalidStatusTransitionException("DELIVERED", "CANCELLED");
        }

        shipment.setStatus(newStatus);
        if (newStatus == ShipmentStatus.DELIVERED) {
            shipment.setDeliveredAt(LocalDateTime.now());
        }

        return shipmentRepositoryPort.save(shipment);
    }
}
