package com.nexusmarket.domain.ports.out;

import com.nexusmarket.domain.models.Shipment;

import java.util.List;
import java.util.Optional;

public interface ShipmentRepositoryPort {

    Shipment save(Shipment shipment);

    Optional<Shipment> findById(Long id);

    Optional<Shipment> findByTrackingNumber(String trackingNumber);

    Optional<Shipment> findByOrderId(Long orderId);

    boolean existsByOrderId(Long orderId);

    boolean existsByTrackingNumber(String trackingNumber);

    List<Shipment> findAll();
}
