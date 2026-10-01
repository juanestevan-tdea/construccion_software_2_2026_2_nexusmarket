package com.nexusmarket.adapters.useCases;

import java.util.List;

import org.springframework.stereotype.Service;

import com.nexusmarket.domain.exceptions.ResourceNotFoundException;
import com.nexusmarket.domain.exceptions.TrackingNotFoundException;
import com.nexusmarket.domain.models.Shipment;
import com.nexusmarket.domain.ports.in.ShipmentUseCasePort;
import com.nexusmarket.domain.ports.out.OrderRepositoryPort;
import com.nexusmarket.domain.ports.out.ShipmentRepositoryPort;
import com.nexusmarket.domain.services.ShipmentCreateService;
import com.nexusmarket.domain.services.ShipmentStatusService;
import com.nexusmarket.domain.valueobjects.ShipmentStatus;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ShipmentUseCaseImpl implements ShipmentUseCasePort {

    private final ShipmentCreateService shipmentCreateService;
    private final ShipmentStatusService shipmentStatusService;
    private final ShipmentRepositoryPort shipmentRepositoryPort;
    private final OrderRepositoryPort orderRepositoryPort;

    @Override
    public Shipment createShipment(Long orderId, Long warehouseId, String trackingNumber) {
        return shipmentCreateService.createShipment(orderId, warehouseId, trackingNumber);
    }

    @Override
    public Shipment getByIdOrThrow(Long id) {
        return shipmentRepositoryPort.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Shipment", id));
    }

    @Override
    public Shipment findByTracking(String trackingNumber) {
        return shipmentRepositoryPort.findByTrackingNumber(trackingNumber)
                .orElseThrow(() -> new TrackingNotFoundException("Shipment", "trackingNumber", trackingNumber));
    }

    @Override
    public Shipment findByOrder(Long orderId) {
        if (!orderRepositoryPort.existsById(orderId)) {
            throw new ResourceNotFoundException("Order", orderId);
        }
        return shipmentRepositoryPort.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Shipment for order", orderId));
    }

    @Override
    public List<Shipment> findAll() {
        return shipmentRepositoryPort.findAll();
    }

    @Override
    public Shipment updateStatus(Long id, ShipmentStatus status) {
        return shipmentStatusService.updateStatus(id, status);
    }
}

