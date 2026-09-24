package com.nexusmarket.catalog.service;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.nexusmarket.catalog.domain.model.Warehouse;
import com.nexusmarket.catalog.domain.model.WarehouseType;
import com.nexusmarket.catalog.domain.repository.WarehouseRepository;
import com.nexusmarket.catalog.dto.request.WarehouseCreateRequest;
import com.nexusmarket.catalog.dto.response.WarehouseResponse;
import com.nexusmarket.catalog.dto.request.WarehouseUpdateRequest;
import com.nexusmarket.common.exception.DuplicateResourceException;
import com.nexusmarket.common.exception.ResourceNotFoundException;
import com.nexusmarket.common.exception.WarehouseCapacityExceededException;
import com.nexusmarket.inventory.domain.model.Inventory;
import com.nexusmarket.inventory.domain.repository.InventoryRepository;

@ExtendWith(MockitoExtension.class)
class WarehouseServiceTest {

    @Mock
    private WarehouseRepository warehouseRepository;

    @Mock
    private InventoryRepository inventoryRepository;

    @InjectMocks
    private WarehouseService warehouseService;

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
    }

    @Test
    void createWarehouse_Success() {
        WarehouseCreateRequest request = WarehouseCreateRequest.builder()
                .name("Main Central Warehouse")
                .location("Bogota")
                .type(WarehouseType.MARKETPLACE)
                .capacity(1000)
                .build();

        when(warehouseRepository.existsByName("Main Central Warehouse")).thenReturn(false);
        when(warehouseRepository.save(any(Warehouse.class))).thenReturn(warehouse);

        WarehouseResponse response = warehouseService.createWarehouse(request);

        assertNotNull(response);
        assertEquals("Main Central Warehouse", response.getName());
        assertEquals(1000, response.getCapacity());
    }

    @Test
    void createWarehouse_ThrowsDuplicateResourceException_WhenNameExists() {
        WarehouseCreateRequest request = WarehouseCreateRequest.builder()
                .name("Main Central Warehouse")
                .build();

        when(warehouseRepository.existsByName("Main Central Warehouse")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> warehouseService.createWarehouse(request));
    }

    @Test
    void updateWarehouse_ThrowsWarehouseCapacityExceededException_WhenCapacityLowerThanStored() {
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(warehouse));

        Inventory inventory = new Inventory();
        inventory.setQuantity(600);
        when(inventoryRepository.findByWarehouse(warehouse)).thenReturn(List.of(inventory));

        WarehouseUpdateRequest request = WarehouseUpdateRequest.builder()
                .capacity(400)
                .build();

        assertThrows(WarehouseCapacityExceededException.class, () -> warehouseService.updateWarehouse(1L, request));
    }

    @Test
    void getWarehouseByIdOrThrow_ThrowsResourceNotFoundException_WhenNotFound() {
        when(warehouseRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> warehouseService.getWarehouseByIdOrThrow(99L));
    }
}
