package com.nexusmarket.inventory.service;

import java.util.Collections;
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

import com.nexusmarket.catalog.domain.model.Product;
import com.nexusmarket.catalog.domain.model.Warehouse;
import com.nexusmarket.catalog.domain.repository.ProductRepository;
import com.nexusmarket.catalog.domain.repository.WarehouseRepository;
import com.nexusmarket.common.exception.BusinessRuleException;
import com.nexusmarket.common.exception.WarehouseCapacityExceededException;
import com.nexusmarket.inventory.domain.model.Inventory;
import com.nexusmarket.inventory.domain.model.InventoryStatus;
import com.nexusmarket.inventory.domain.repository.InventoryRepository;
import com.nexusmarket.inventory.dto.InventoryCreateRequest;
import com.nexusmarket.inventory.dto.InventoryResponse;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private WarehouseRepository warehouseRepository;

    @InjectMocks
    private InventoryService inventoryService;

    private Product product;
    private Warehouse warehouse;

    @BeforeEach
    void setUp() {
        product = Product.builder().id(1L).name("Mouse").sku("MS-1").build();
        warehouse = Warehouse.builder().id(1L).name("Main Warehouse").capacity(100).build();
    }

    @Test
    void createInventory_Success() {
        InventoryCreateRequest request = InventoryCreateRequest.builder()
                .productId(1L)
                .warehouseId(1L)
                .quantity(50)
                .build();

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(warehouse));
        when(inventoryRepository.findByWarehouse(warehouse)).thenReturn(Collections.emptyList());
        when(inventoryRepository.save(any(Inventory.class))).thenAnswer(i -> {
            Inventory inv = i.getArgument(0);
            inv.setId(10L);
            return inv;
        });

        InventoryResponse response = inventoryService.createInventory(request);

        assertNotNull(response);
        assertEquals(50, response.getQuantity());
        assertEquals(InventoryStatus.AVAILABLE, response.getStatus());
    }

    @Test
    void createInventory_ThrowsBusinessRuleException_WhenQuantityNegative() {
        assertThrows(BusinessRuleException.class, ()
                -> inventoryService.createInventory(1L, 1L, -5));
    }

    @Test
    void createInventory_ThrowsWarehouseCapacityExceededException_WhenCapacityExceeded() {
        InventoryCreateRequest request = InventoryCreateRequest.builder()
                .productId(1L)
                .warehouseId(1L)
                .quantity(150)
                .build();

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(warehouse));
        when(inventoryRepository.findByWarehouse(warehouse)).thenReturn(Collections.emptyList());

        assertThrows(WarehouseCapacityExceededException.class, () -> inventoryService.createInventory(request));
    }

    @Test
    void reserve_ThrowsBusinessRuleException_WhenDamaged() {
        Inventory inv = new Inventory();
        inv.setId(1L);
        inv.setStatus(InventoryStatus.DAMAGED);
        inv.setQuantity(20);

        when(inventoryRepository.findById(1L)).thenReturn(Optional.of(inv));

        assertThrows(BusinessRuleException.class, () -> inventoryService.reserve(1L, 5));
    }

    @Test
    void reserve_ThrowsBusinessRuleException_WhenInsufficientQuantity() {
        Inventory inv = new Inventory();
        inv.setId(1L);
        inv.setStatus(InventoryStatus.AVAILABLE);
        inv.setQuantity(4);

        when(inventoryRepository.findById(1L)).thenReturn(Optional.of(inv));

        assertThrows(BusinessRuleException.class, () -> inventoryService.reserve(1L, 10));
    }

    @Test
    void markAsDamaged_Success() {
        Inventory inv = new Inventory();
        inv.setId(1L);
        inv.setStatus(InventoryStatus.AVAILABLE);
        inv.setQuantity(10);

        when(inventoryRepository.findById(1L)).thenReturn(Optional.of(inv));
        when(inventoryRepository.save(any(Inventory.class))).thenAnswer(i -> i.getArgument(0));

        Inventory result = inventoryService.markAsDamaged(1L);

        assertNotNull(result);
        assertEquals(InventoryStatus.DAMAGED, result.getStatus());
    }
}
