package com.nexusmarket.catalog.service;

import com.nexusmarket.catalog.domain.model.Category;
import com.nexusmarket.catalog.domain.model.Product;
import com.nexusmarket.catalog.domain.repository.CategoryRepository;
import com.nexusmarket.catalog.domain.repository.ProductRepository;
import com.nexusmarket.catalog.dto.ProductCreateRequest;
import com.nexusmarket.catalog.dto.ProductResponse;
import com.nexusmarket.catalog.dto.ProductUpdateRequest;
import com.nexusmarket.exception.BusinessRuleException;
import com.nexusmarket.exception.DuplicateResourceException;
import com.nexusmarket.exception.ProductNotAvailableException;
import com.nexusmarket.exception.ResourceNotFoundException;
import com.nexusmarket.inventory.domain.model.Inventory;
import com.nexusmarket.inventory.domain.model.InventoryStatus;
import com.nexusmarket.inventory.domain.repository.InventoryRepository;
import com.nexusmarket.users.domain.model.Seller;
import com.nexusmarket.users.domain.model.UserRole;
import com.nexusmarket.users.domain.repository.SellerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final SellerRepository sellerRepository;
    private final InventoryRepository inventoryRepository;

    @Transactional
    public ProductResponse createProduct(ProductCreateRequest request) {
        if (request.getPrice() == null || request.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessRuleException("Price must be greater than zero");
        }

        if (productRepository.existsBySku(request.getSku())) {
            throw new DuplicateResourceException("Product", "sku", request.getSku());
        }

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", request.getCategoryId()));

        Seller seller = sellerRepository.findById(request.getSellerId())
                .orElseThrow(() -> new ResourceNotFoundException("Seller", request.getSellerId()));

        if (seller.getUser() == null || seller.getUser().getRole() != UserRole.SELLER) {
            throw new BusinessRuleException("User associated with seller does not have SELLER role");
        }

        if (Boolean.FALSE.equals(seller.getActive())) {
            throw new BusinessRuleException("Seller must be active to publish products");
        }

        Product product = Product.builder()
                .sku(request.getSku())
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .type(request.getType())
                .active(true)
                .category(category)
                .seller(seller)
                .build();

        return ProductResponse.fromEntity(productRepository.save(product));
    }

    @Transactional(readOnly = true)
    public Product getProductByIdOrThrow(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));
    }

    @Transactional(readOnly = true)
    public ProductResponse getProductResponseById(Long id) {
        return ProductResponse.fromEntity(getProductByIdOrThrow(id));
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> findAll() {
        return productRepository.findAll().stream()
                .map(ProductResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> findByCategory(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", categoryId));
        return productRepository.findByCategory(category).stream()
                .map(ProductResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> findBySeller(Long sellerId) {
        Seller seller = sellerRepository.findById(sellerId)
                .orElseThrow(() -> new ResourceNotFoundException("Seller", sellerId));
        return productRepository.findBySeller(seller).stream()
                .map(ProductResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> findByPriceRange(BigDecimal min, BigDecimal max) {
        return productRepository.findByPriceBetween(min, max).stream()
                .map(ProductResponse::fromEntity)
                .toList();
    }

    @Transactional
    public ProductResponse updateProduct(Long id, ProductUpdateRequest request) {
        Product product = getProductByIdOrThrow(id);

        if (request.getName() != null && !request.getName().isBlank()) {
            product.setName(request.getName());
        }
        if (request.getDescription() != null) {
            product.setDescription(request.getDescription());
        }
        if (request.getPrice() != null) {
            if (request.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessRuleException("Price must be greater than zero");
            }
            product.setPrice(request.getPrice());
        }
        if (request.getType() != null) {
            product.setType(request.getType());
        }
        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", request.getCategoryId()));
            product.setCategory(category);
        }

        return ProductResponse.fromEntity(productRepository.save(product));
    }

    @Transactional
    public ProductResponse deactivateProduct(Long id) {
        Product product = getProductByIdOrThrow(id);

        // Validar que no tenga stock reservado en inventario
        List<Inventory> inventories = inventoryRepository.findByProduct(product);
        boolean hasReservedStock = inventories.stream()
                .anyMatch(inv -> inv.getStatus() == InventoryStatus.RESERVED);

        if (hasReservedStock) {
            throw new ProductNotAvailableException(
                    String.format("Cannot deactivate product with id '%s' because it has reserved stock in inventory", id)
            );
        }

        product.setActive(false);
        return ProductResponse.fromEntity(productRepository.save(product));
    }
}
