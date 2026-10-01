package com.nexusmarket.domain.services;

import com.nexusmarket.domain.exceptions.BusinessRuleException;
import com.nexusmarket.domain.exceptions.DuplicateResourceException;
import com.nexusmarket.domain.exceptions.ProductNotAvailableException;
import com.nexusmarket.domain.exceptions.ResourceNotFoundException;
import com.nexusmarket.domain.models.Category;
import com.nexusmarket.domain.models.Inventory;
import com.nexusmarket.domain.models.Product;
import com.nexusmarket.domain.models.Seller;
import com.nexusmarket.domain.models.User;
import com.nexusmarket.domain.ports.out.CategoryRepositoryPort;
import com.nexusmarket.domain.ports.out.InventoryRepositoryPort;
import com.nexusmarket.domain.ports.out.ProductRepositoryPort;
import com.nexusmarket.domain.ports.out.SellerRepositoryPort;
import com.nexusmarket.domain.ports.out.UserRepositoryPort;
import com.nexusmarket.domain.valueobjects.InventoryStatus;
import com.nexusmarket.domain.valueobjects.ProductType;
import com.nexusmarket.domain.valueobjects.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductDomainService {

    private final ProductRepositoryPort productRepositoryPort;
    private final CategoryRepositoryPort categoryRepositoryPort;
    private final SellerRepositoryPort sellerRepositoryPort;
    private final UserRepositoryPort userRepositoryPort;
    private final InventoryRepositoryPort inventoryRepositoryPort;

    @Transactional
    public Product createProduct(String sku, String name, String description, BigDecimal price,
            ProductType type, Long sellerId, Long categoryId) {
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessRuleException("Price must be greater than zero");
        }

        if (productRepositoryPort.existsBySku(sku)) {
            throw new DuplicateResourceException("Product", "sku", sku);
        }

        Category category = categoryRepositoryPort.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", categoryId));

        Seller seller = sellerRepositoryPort.findById(sellerId)
                .orElseThrow(() -> new ResourceNotFoundException("Seller", sellerId));

        User user = null;
        if (seller.getUserId() != null) {
            user = userRepositoryPort.findById(seller.getUserId()).orElse(null);
        }

        if (user == null || user.getRole() != UserRole.SELLER) {
            throw new BusinessRuleException("User associated with seller does not have SELLER role");
        }

        if (Boolean.FALSE.equals(seller.getActive())) {
            throw new BusinessRuleException("Seller must be active to publish products");
        }

        Product product = Product.builder()
                .sku(sku)
                .name(name)
                .description(description)
                .price(price)
                .type(type)
                .active(true)
                .categoryId(category.getId())
                .categoryName(category.getName())
                .sellerId(seller.getId())
                .sellerCompanyName(seller.getCompanyName())
                .build();

        return productRepositoryPort.save(product);
    }

    @Transactional
    public Product updateProduct(Long id, String name, String description, BigDecimal price,
            ProductType type, Long categoryId) {
        Product product = productRepositoryPort.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));

        if (name != null && !name.isBlank()) {
            product.setName(name);
        }
        if (description != null) {
            product.setDescription(description);
        }
        if (price != null) {
            if (price.compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessRuleException("Price must be greater than zero");
            }
            product.setPrice(price);
        }
        if (type != null) {
            product.setType(type);
        }
        if (categoryId != null) {
            Category category = categoryRepositoryPort.findById(categoryId)
                    .orElseThrow(() -> new ResourceNotFoundException("Category", categoryId));
            product.setCategoryId(category.getId());
            product.setCategoryName(category.getName());
        }

        return productRepositoryPort.save(product);
    }

    @Transactional
    public Product deactivateProduct(Long id) {
        Product product = productRepositoryPort.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));

        List<Inventory> inventories = inventoryRepositoryPort.findByProductId(id);
        boolean hasReservedStock = inventories.stream()
                .anyMatch(inv -> inv.getStatus() == InventoryStatus.RESERVED);

        if (hasReservedStock) {
            throw new ProductNotAvailableException(
                    String.format("Cannot deactivate product with id '%s' because it has reserved stock in inventory", id)
            );
        }

        product.setActive(false);
        return productRepositoryPort.save(product);
    }
}

