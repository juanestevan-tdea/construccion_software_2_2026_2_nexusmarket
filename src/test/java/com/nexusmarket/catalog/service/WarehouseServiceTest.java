package com.nexusmarket.catalog.service;

import com.nexusmarket.adapters.useCases.WarehouseUseCaseImpl;
import com.nexusmarket.domain.exceptions.DuplicateResourceException;
import com.nexusmarket.domain.exceptions.ResourceNotFoundException;
import com.nexusmarket.domain.exceptions.WarehouseCapacityExceededException;
import com.nexusmarket.domain.models.Inventory;
import com.nexusmarket.domain.models.Warehouse;
import com.nexusmarket.domain.ports.in.WarehouseUseCasePort;
import com.nexusmarket.domain.ports.out.InventoryRepositoryPort;
import com.nexusmarket.domain.ports.out.WarehouseRepositoryPort;
import com.nexusmarket.domain.services.WarehouseDomainService;
import com.nexusmarket.domain.valueobjects.WarehouseType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WarehouseServiceTest {

    @Mock
    private WarehouseRepositoryPort warehouseRepositoryPort;

    @Mock
    private InventoryRepositoryPort inventoryRepositoryPort;

    private WarehouseUseCasePort warehouseUseCase;

    private Warehouse warehouse;

    @BeforeEach
    void setUp() {
        warehouse = Warehouse.builder()
                .id(1L)
                .name("Main Central Warehouse")
                .location("Bogota")
                .type(WarehouseType.MARKETPLACE)
                .capacity(1000)
                .build();

        WarehouseDomainService domainService = new WarehouseDomainService(warehouseRepositoryPort, inventoryRepositoryPort);
        warehouseUseCase = new WarehouseUseCaseImpl(domainService, warehouseRepositoryPort);
    }

    @Test
    void createWarehouse_Success() {
        when(warehouseRepositoryPort.existsByName("Main Central Warehouse")).thenReturn(false);
        when(warehouseRepositoryPort.save(any(Warehouse.class))).thenReturn(warehouse);

        Warehouse response = warehouseUseCase.createWarehouse("Main Central Warehouse", "Bogota", WarehouseType.MARKETPLACE, 1000);

        assertNotNull(response);
        assertEquals("Main Central Warehouse", response.getName());
        assertEquals(1000, response.getCapacity());
    }

    @Test
    void createWarehouse_ThrowsDuplicateResourceException_WhenNameExists() {
        when(warehouseRepositoryPort.existsByName("Main Central Warehouse")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> warehouseUseCase.createWarehouse("Main Central Warehouse", "Bogota", WarehouseType.MARKETPLACE, 1000));
    }

    @Test
    void updateWarehouse_ThrowsWarehouseCapacityExceededException_WhenCapacityLowerThanStored() {
        when(warehouseRepositoryPort.findById(1L)).thenReturn(Optional.of(warehouse));

        Inventory inventory = new Inventory();
        inventory.setQuantity(600);
        when(inventoryRepositoryPort.findByWarehouseId(1L)).thenReturn(List.of(inventory));

        assertThrows(WarehouseCapacityExceededException.class, () -> warehouseUseCase.updateWarehouse(1L, null, null, null, 400));
    }

    @Test
    void getWarehouseByIdOrThrow_ThrowsResourceNotFoundException_WhenNotFound() {
        when(warehouseRepositoryPort.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> warehouseUseCase.getWarehouseByIdOrThrow(99L));
    }
}


