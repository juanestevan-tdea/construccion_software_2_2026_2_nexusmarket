package com.nexusmarket.inventory.service;

import com.nexusmarket.adapters.useCases.InventoryUseCaseImpl;
import com.nexusmarket.common.exception.BusinessRuleException;
import com.nexusmarket.common.exception.WarehouseCapacityExceededException;
import com.nexusmarket.domain.models.Inventory;
import com.nexusmarket.domain.models.Product;
import com.nexusmarket.domain.models.Warehouse;
import com.nexusmarket.domain.ports.in.InventoryUseCasePort;
import com.nexusmarket.domain.ports.out.InventoryRepositoryPort;
import com.nexusmarket.domain.ports.out.ProductRepositoryPort;
import com.nexusmarket.domain.ports.out.WarehouseRepositoryPort;
import com.nexusmarket.domain.services.InventoryConsultService;
import com.nexusmarket.domain.services.InventoryManagementService;
import com.nexusmarket.domain.valueobjects.InventoryStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private InventoryRepositoryPort inventoryRepositoryPort;

    @Mock
    private ProductRepositoryPort productRepositoryPort;

    @Mock
    private WarehouseRepositoryPort warehouseRepositoryPort;

    private InventoryUseCasePort inventoryUseCase;

    private Product product;
    private Warehouse warehouse;

    @BeforeEach
    void setUp() {
        product = Product.builder().id(1L).name("Mouse").sku("MS-1").build();
        warehouse = Warehouse.builder().id(1L).name("Main Warehouse").capacity(100).build();

        InventoryManagementService managementService = new InventoryManagementService(
                inventoryRepositoryPort, productRepositoryPort, warehouseRepositoryPort);
        InventoryConsultService consultService = new InventoryConsultService(
                inventoryRepositoryPort, productRepositoryPort, warehouseRepositoryPort);
        inventoryUseCase = new InventoryUseCaseImpl(managementService, consultService);
    }

    @Test
    void createInventory_Success() {
        when(productRepositoryPort.findById(1L)).thenReturn(Optional.of(product));
        when(warehouseRepositoryPort.findById(1L)).thenReturn(Optional.of(warehouse));
        when(inventoryRepositoryPort.findByWarehouseId(1L)).thenReturn(Collections.emptyList());
        when(inventoryRepositoryPort.save(any(Inventory.class))).thenAnswer(i -> {
            Inventory inv = i.getArgument(0);
            inv.setId(10L);
            return inv;
        });

        Inventory response = inventoryUseCase.createInventory(1L, 1L, 50);

        assertNotNull(response);
        assertEquals(50, response.getQuantity());
        assertEquals(InventoryStatus.AVAILABLE, response.getStatus());
    }

    @Test
    void createInventory_ThrowsBusinessRuleException_WhenQuantityNegative() {
        assertThrows(BusinessRuleException.class, ()
                -> inventoryUseCase.createInventory(1L, 1L, -5));
    }

    @Test
    void createInventory_ThrowsWarehouseCapacityExceededException_WhenCapacityExceeded() {
        when(productRepositoryPort.findById(1L)).thenReturn(Optional.of(product));
        when(warehouseRepositoryPort.findById(1L)).thenReturn(Optional.of(warehouse));
        when(inventoryRepositoryPort.findByWarehouseId(1L)).thenReturn(Collections.emptyList());

        assertThrows(WarehouseCapacityExceededException.class, ()
                -> inventoryUseCase.createInventory(1L, 1L, 150));
    }

    @Test
    void reserve_ThrowsBusinessRuleException_WhenDamaged() {
        Inventory inv = new Inventory();
        inv.setId(1L);
        inv.setStatus(InventoryStatus.DAMAGED);
        inv.setQuantity(20);

        when(inventoryRepositoryPort.findById(1L)).thenReturn(Optional.of(inv));

        assertThrows(BusinessRuleException.class, () -> inventoryUseCase.reserve(1L, 5));
    }

    @Test
    void reserve_ThrowsBusinessRuleException_WhenInsufficientQuantity() {
        Inventory inv = new Inventory();
        inv.setId(1L);
        inv.setStatus(InventoryStatus.AVAILABLE);
        inv.setQuantity(4);

        when(inventoryRepositoryPort.findById(1L)).thenReturn(Optional.of(inv));

        assertThrows(BusinessRuleException.class, () -> inventoryUseCase.reserve(1L, 10));
    }

    @Test
    void markAsDamaged_Success() {
        Inventory inv = new Inventory();
        inv.setId(1L);
        inv.setStatus(InventoryStatus.AVAILABLE);
        inv.setQuantity(10);

        when(inventoryRepositoryPort.findById(1L)).thenReturn(Optional.of(inv));
        when(inventoryRepositoryPort.save(any(Inventory.class))).thenAnswer(i -> i.getArgument(0));

        Inventory result = inventoryUseCase.markAsDamaged(1L);

        assertNotNull(result);
        assertEquals(InventoryStatus.DAMAGED, result.getStatus());
    }
}

