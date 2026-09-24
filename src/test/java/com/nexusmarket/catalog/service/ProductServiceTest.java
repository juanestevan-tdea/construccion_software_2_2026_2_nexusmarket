package com.nexusmarket.catalog.service;

import com.nexusmarket.catalog.domain.model.Category;
import com.nexusmarket.catalog.domain.model.Product;
import com.nexusmarket.catalog.domain.model.ProductType;
import com.nexusmarket.catalog.domain.repository.CategoryRepository;
import com.nexusmarket.catalog.domain.repository.ProductRepository;
import com.nexusmarket.catalog.dto.request.ProductCreateRequest;
import com.nexusmarket.catalog.dto.response.ProductResponse;
import com.nexusmarket.common.exception.BusinessRuleException;
import com.nexusmarket.common.exception.DuplicateResourceException;
import com.nexusmarket.common.exception.ProductNotAvailableException;
import com.nexusmarket.inventory.domain.model.Inventory;
import com.nexusmarket.inventory.domain.model.InventoryStatus;
import com.nexusmarket.inventory.domain.repository.InventoryRepository;
import com.nexusmarket.users.domain.model.Seller;
import com.nexusmarket.users.domain.model.User;
import com.nexusmarket.users.domain.model.UserRole;
import com.nexusmarket.users.domain.repository.SellerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
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
    private ProductRepository productRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private SellerRepository sellerRepository;
    @Mock
    private InventoryRepository inventoryRepository;

    @InjectMocks
    private ProductService productService;

    private Seller activeSeller;
    private Category category;

    @BeforeEach
    void setUp() {
        User sellerUser = User.builder().id(1L).email("seller@test.com").role(UserRole.SELLER).build();
        activeSeller = Seller.builder().id(1L).user(sellerUser).taxId("TAX123").active(true).build();
        category = Category.builder().id(1L).name("Electronics").build();
    }

    @Test
    void createProduct_Success() {
        ProductCreateRequest request = ProductCreateRequest.builder()
                .sku("SKU-100")
                .name("Smartphone")
                .price(BigDecimal.valueOf(499.99))
                .type(ProductType.PHYSICAL)
                .sellerId(1L)
                .categoryId(1L)
                .build();

        when(productRepository.existsBySku("SKU-100")).thenReturn(false);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(sellerRepository.findById(1L)).thenReturn(Optional.of(activeSeller));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> {
            Product p = invocation.getArgument(0);
            p.setId(10L);
            return p;
        });

        ProductResponse response = productService.createProduct(request);

        assertNotNull(response);
        assertEquals("SKU-100", response.getSku());
        assertEquals("Smartphone", response.getName());
    }

    @Test
    void createProduct_ThrowsException_WhenPriceIsZeroOrNegative() {
        ProductCreateRequest request = ProductCreateRequest.builder()
                .sku("SKU-100")
                .name("Smartphone")
                .price(BigDecimal.ZERO)
                .type(ProductType.PHYSICAL)
                .sellerId(1L)
                .categoryId(1L)
                .build();

        assertThrows(BusinessRuleException.class, () -> productService.createProduct(request));
    }

    @Test
    void createProduct_ThrowsException_WhenSkuExists() {
        ProductCreateRequest request = ProductCreateRequest.builder()
                .sku("SKU-100")
                .name("Smartphone")
                .price(BigDecimal.valueOf(100))
                .type(ProductType.PHYSICAL)
                .sellerId(1L)
                .categoryId(1L)
                .build();

        when(productRepository.existsBySku("SKU-100")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> productService.createProduct(request));
    }

    @Test
    void deactivateProduct_ThrowsException_WhenStockIsReserved() {
        Product product = Product.builder().id(5L).sku("SKU-5").active(true).build();
        when(productRepository.findById(5L)).thenReturn(Optional.of(product));

        Inventory inventory = new Inventory();
        inventory.setStatus(InventoryStatus.RESERVED);
        inventory.setQuantity(5);
        when(inventoryRepository.findByProduct(product)).thenReturn(List.of(inventory));

        assertThrows(ProductNotAvailableException.class, () -> productService.deactivateProduct(5L));
    }
}
