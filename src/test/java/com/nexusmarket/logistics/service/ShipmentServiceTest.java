package com.nexusmarket.logistics.service;

import com.nexusmarket.exception.InvalidStatusTransitionException;
import com.nexusmarket.exception.TrackingNotFoundException;
import com.nexusmarket.logistics.domain.model.Shipment;
import com.nexusmarket.logistics.domain.model.ShipmentStatus;
import com.nexusmarket.logistics.domain.repository.ShipmentRepository;
import com.nexusmarket.logistics.dto.ShipmentResponse;
import com.nexusmarket.logistics.dto.ShipmentStatusUpdateRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShipmentServiceTest {

    @Mock
    private ShipmentRepository shipmentRepository;

    @InjectMocks
    private ShipmentService shipmentService;

    @Test
    void findByTracking_ThrowsException_WhenNotFound() {
        when(shipmentRepository.findByTrackingNumber("TRK-UNKNOWN")).thenReturn(Optional.empty());

        assertThrows(TrackingNotFoundException.class, () -> shipmentService.findByTracking("TRK-UNKNOWN"));
    }

    @Test
    void updateStatus_ThrowsException_WhenCancellingDeliveredShipment() {
        Shipment shipment = Shipment.builder()
                .id(1L)
                .status(ShipmentStatus.DELIVERED)
                .build();
        when(shipmentRepository.findById(1L)).thenReturn(Optional.of(shipment));

        ShipmentStatusUpdateRequest request = ShipmentStatusUpdateRequest.builder()
                .status(ShipmentStatus.CANCELLED)
                .build();

        assertThrows(InvalidStatusTransitionException.class, () -> shipmentService.updateStatus(1L, request));
    }

    @Test
    void updateStatus_Success() {
        Shipment shipment = Shipment.builder()
                .id(1L)
                .status(ShipmentStatus.PENDING)
                .build();
        when(shipmentRepository.findById(1L)).thenReturn(Optional.of(shipment));
        when(shipmentRepository.save(any(Shipment.class))).thenAnswer(i -> i.getArgument(0));

        ShipmentStatusUpdateRequest request = ShipmentStatusUpdateRequest.builder()
                .status(ShipmentStatus.IN_TRANSIT)
                .build();

        ShipmentResponse response = shipmentService.updateStatus(1L, request);

        assertNotNull(response);
        assertEquals(ShipmentStatus.IN_TRANSIT, response.getStatus());
    }
}
