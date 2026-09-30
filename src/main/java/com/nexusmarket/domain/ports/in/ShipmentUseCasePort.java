package com.nexusmarket.domain.ports.in;

import com.nexusmarket.domain.models.Shipment;
import com.nexusmarket.domain.valueobjects.ShipmentStatus;

import java.util.List;

public interface ShipmentUseCasePort {

    Shipment createShipment(Long orderId, Long warehouseId, String trackingNumber);

    Shipment getByIdOrThrow(Long id);

    Shipment findByTracking(String trackingNumber);

    Shipment findByOrder(Long orderId);

    List<Shipment> findAll();

    Shipment updateStatus(Long id, ShipmentStatus status);
}
