package com.nexusmarket.catalog.service;

import com.nexusmarket.adapters.useCases.ProductUseCaseImpl;
import com.nexusmarket.common.exception.BusinessRuleException;
import com.nexusmarket.common.exception.DuplicateResourceException;
import com.nexusmarket.common.exception.ProductNotAvailableException;
import com.nexusmarket.domain.models.Category;
import com.nexusmarket.domain.models.Inventory;
import com.nexusmarket.domain.models.Product;
import com.nexusmarket.domain.models.Seller;
import com.nexusmarket.domain.models.User;
import com.nexusmarket.domain.ports.in.ProductUseCasePort;
import com.nexusmarket.domain.ports.out.CategoryRepositoryPort;
import com.nexusmarket.domain.ports.out.InventoryRepositoryPort;
import com.nexusmarket.domain.ports.out.ProductRepositoryPort;
import com.nexusmarket.domain.ports.out.SellerRepositoryPort;
import com.nexusmarket.domain.ports.out.UserRepositoryPort;
import com.nexusmarket.domain.services.ProductDomainService;
import com.nexusmarket.domain.valueobjects.InventoryStatus;
import com.nexusmarket.domain.valueobjects.ProductType;
import com.nexusmarket.domain.valueobjects.UserRole;
import com.nexusmarket.domain.valueobjects.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepositoryPort productRepositoryPort;
    @Mock
    private CategoryRepositoryPort categoryRepositoryPort;
    @Mock
    private SellerRepositoryPort sellerRepositoryPort;
    @Mock
    private UserRepositoryPort userRepositoryPort;
    @Mock
    private InventoryRepositoryPort inventoryRepositoryPort;

    private ProductUseCasePort productUseCase;

    private Seller activeSeller;
    private User sellerUser;
    private Category category;

    @BeforeEach
    void setUp() {
        sellerUser = User.builder().id(1L).email("seller@test.com").role(UserRole.SELLER).status(UserStatus.ACTIVE).build();
        activeSeller = Seller.builder().id(1L).userId(1L).companyName("Tienda Nexus").taxId("TAX123").active(true).build();
        category = Category.builder().id(1L).name("Electronics").build();

        ProductDomainService domainService = new ProductDomainService(
                productRepositoryPort, categoryRepositoryPort, sellerRepositoryPort, userRepositoryPort, inventoryRepositoryPort);
        productUseCase = new ProductUseCaseImpl(domainService, productRepositoryPort);
    }

    @Test
    void createProduct_Success() {
        when(productRepositoryPort.existsBySku("SKU-100")).thenReturn(false);
        when(categoryRepositoryPort.findById(1L)).thenReturn(Optional.of(category));
        when(sellerRepositoryPort.findById(1L)).thenReturn(Optional.of(activeSeller));
        when(userRepositoryPort.findById(1L)).thenReturn(Optional.of(sellerUser));
        when(productRepositoryPort.save(any(Product.class))).thenAnswer(invocation -> {
            Product p = invocation.getArgument(0);
            p.setId(10L);
            return p;
        });

        Product response = productUseCase.createProduct("SKU-100", "Smartphone", null, BigDecimal.valueOf(499.99), ProductType.PHYSICAL, 1L, 1L);

        assertNotNull(response);
        assertEquals("SKU-100", response.getSku());
        assertEquals("Smartphone", response.getName());
    }

    @Test
    void createProduct_ThrowsException_WhenPriceIsZeroOrNegative() {
        assertThrows(BusinessRuleException.class, () -> productUseCase.createProduct("SKU-100", "Smartphone", null, BigDecimal.ZERO, ProductType.PHYSICAL, 1L, 1L));
    }

    @Test
    void createProduct_ThrowsException_WhenSkuExists() {
        when(productRepositoryPort.existsBySku("SKU-100")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> productUseCase.createProduct("SKU-100", "Smartphone", null, BigDecimal.valueOf(100), ProductType.PHYSICAL, 1L, 1L));
    }

    @Test
    void deactivateProduct_ThrowsException_WhenStockIsReserved() {
        Product product = Product.builder().id(5L).sku("SKU-5").active(true).build();
        when(productRepositoryPort.findById(5L)).thenReturn(Optional.of(product));

        Inventory inventory = new Inventory();
        inventory.setStatus(InventoryStatus.RESERVED);
        inventory.setQuantity(5);
        when(inventoryRepositoryPort.findByProductId(5L)).thenReturn(List.of(inventory));

        assertThrows(ProductNotAvailableException.class, () -> productUseCase.deactivateProduct(5L));
    }
}
