package com.nexusmarket.logistics.service;

import com.nexusmarket.adapters.useCases.ShipmentUseCaseImpl;
import com.nexusmarket.catalog.domain.repository.WarehouseRepository;
import com.nexusmarket.common.exception.InvalidStatusTransitionException;
import com.nexusmarket.common.exception.TrackingNotFoundException;
import com.nexusmarket.domain.models.Shipment;
import com.nexusmarket.domain.ports.in.ShipmentUseCasePort;
import com.nexusmarket.domain.ports.out.OrderRepositoryPort;
import com.nexusmarket.domain.ports.out.ShipmentRepositoryPort;
import com.nexusmarket.domain.services.ShipmentCreateService;
import com.nexusmarket.domain.services.ShipmentStatusService;
import com.nexusmarket.domain.valueobjects.ShipmentStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShipmentServiceTest {

    @Mock
    private ShipmentRepositoryPort shipmentRepositoryPort;

    @Mock
    private OrderRepositoryPort orderRepositoryPort;

    @Mock
    private WarehouseRepository warehouseRepository;

    private ShipmentUseCasePort shipmentUseCase;

    @BeforeEach
    void setUp() {
        ShipmentCreateService createService = new ShipmentCreateService(shipmentRepositoryPort, orderRepositoryPort, warehouseRepository);
        ShipmentStatusService statusService = new ShipmentStatusService(shipmentRepositoryPort);
        shipmentUseCase = new ShipmentUseCaseImpl(createService, statusService, shipmentRepositoryPort, orderRepositoryPort);
    }

    @Test
    void findByTracking_ThrowsException_WhenNotFound() {
        when(shipmentRepositoryPort.findByTrackingNumber("TRK-UNKNOWN")).thenReturn(Optional.empty());

        assertThrows(TrackingNotFoundException.class, () -> shipmentUseCase.findByTracking("TRK-UNKNOWN"));
    }

    @Test
    void updateStatus_ThrowsException_WhenCancellingDeliveredShipment() {
        Shipment shipment = Shipment.builder()
                .id(1L)
                .status(ShipmentStatus.DELIVERED)
                .build();
        when(shipmentRepositoryPort.findById(1L)).thenReturn(Optional.of(shipment));

        assertThrows(InvalidStatusTransitionException.class, () -> shipmentUseCase.updateStatus(1L, ShipmentStatus.CANCELLED));
    }

    @Test
    void updateStatus_Success() {
        Shipment shipment = Shipment.builder()
                .id(1L)
                .status(ShipmentStatus.PENDING)
                .build();
        when(shipmentRepositoryPort.findById(1L)).thenReturn(Optional.of(shipment));
        when(shipmentRepositoryPort.save(any(Shipment.class))).thenAnswer(i -> i.getArgument(0));

        Shipment response = shipmentUseCase.updateStatus(1L, ShipmentStatus.IN_TRANSIT);

        assertNotNull(response);
        assertEquals(ShipmentStatus.IN_TRANSIT, response.getStatus());
    }
}

