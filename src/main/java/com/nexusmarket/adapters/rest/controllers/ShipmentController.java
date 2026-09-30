package com.nexusmarket.adapters.rest.controllers;

import com.nexusmarket.adapters.rest.dtos.requests.ShipmentCreateRequestDTO;
import com.nexusmarket.adapters.rest.dtos.requests.ShipmentStatusUpdateRequestDTO;
import com.nexusmarket.adapters.rest.dtos.responses.ShipmentResponseDTO;
import com.nexusmarket.adapters.rest.mappers.ShipmentRestMapper;
import com.nexusmarket.domain.models.Shipment;
import com.nexusmarket.domain.ports.in.ShipmentUseCasePort;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/shipments")
@RequiredArgsConstructor
public class ShipmentController {

    private final ShipmentUseCasePort shipmentUseCasePort;

    @PostMapping
    public ResponseEntity<ShipmentResponseDTO> createShipment(@Valid @RequestBody ShipmentCreateRequestDTO request) {
        Shipment shipment = shipmentUseCasePort.createShipment(
                request.getOrderId(),
                request.getWarehouseId(),
                request.getTrackingNumber()
        );
        return new ResponseEntity<>(ShipmentRestMapper.toResponseDTO(shipment), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ShipmentResponseDTO> getShipmentById(@PathVariable Long id) {
        Shipment shipment = shipmentUseCasePort.getByIdOrThrow(id);
        return ResponseEntity.ok(ShipmentRestMapper.toResponseDTO(shipment));
    }

    @GetMapping("/tracking/{trackingNumber}")
    public ResponseEntity<ShipmentResponseDTO> getShipmentByTracking(@PathVariable String trackingNumber) {
        Shipment shipment = shipmentUseCasePort.findByTracking(trackingNumber);
        return ResponseEntity.ok(ShipmentRestMapper.toResponseDTO(shipment));
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<ShipmentResponseDTO> getShipmentByOrder(@PathVariable Long orderId) {
        Shipment shipment = shipmentUseCasePort.findByOrder(orderId);
        return ResponseEntity.ok(ShipmentRestMapper.toResponseDTO(shipment));
    }

    @GetMapping
    public ResponseEntity<List<ShipmentResponseDTO>> getAllShipments() {
        List<ShipmentResponseDTO> list = shipmentUseCasePort.findAll().stream()
                .map(ShipmentRestMapper::toResponseDTO)
                .toList();
        return ResponseEntity.ok(list);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ShipmentResponseDTO> updateStatus(@PathVariable Long id,
            @Valid @RequestBody ShipmentStatusUpdateRequestDTO request) {
        Shipment shipment = shipmentUseCasePort.updateStatus(id, request.getStatus());
        return ResponseEntity.ok(ShipmentRestMapper.toResponseDTO(shipment));
    }
}
