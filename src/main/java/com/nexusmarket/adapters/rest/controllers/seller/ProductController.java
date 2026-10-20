package com.nexusmarket.adapters.rest.controllers.seller;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nexusmarket.adapters.rest.dtos.requests.ProductCreateRequestDTO;
import com.nexusmarket.adapters.rest.dtos.requests.ProductUpdateRequestDTO;
import com.nexusmarket.adapters.rest.dtos.responses.ProductResponseDTO;
import com.nexusmarket.adapters.rest.mappers.ProductRestMapper;
import com.nexusmarket.domain.exceptions.ResourceNotFoundException;
import com.nexusmarket.domain.models.Product;
import com.nexusmarket.domain.models.Seller;
import com.nexusmarket.domain.ports.in.seller.ProductUseCasePort;
import com.nexusmarket.domain.ports.out.SellerRepositoryPort;
import com.nexusmarket.infrastructure.security.UserDetailsAdapter;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/seller/products")
@PreAuthorize("hasRole('SELLER')")
@RequiredArgsConstructor
public class ProductController {

    private final ProductUseCasePort productUseCasePort;
    private final SellerRepositoryPort sellerRepositoryPort;

    @PostMapping
    public ResponseEntity<ProductResponseDTO> createProduct(
            @AuthenticationPrincipal UserDetailsAdapter principal,
            @Valid @RequestBody ProductCreateRequestDTO request) {

        Seller seller = currentSeller(principal);
        Product product = productUseCasePort.createProduct(
                request.getSku(),
                request.getName(),
                request.getDescription(),
                request.getPrice(),
                request.getType(),
                seller.getId(),
                request.getCategoryId()
        );
        return new ResponseEntity<>(ProductRestMapper.toResponseDTO(product), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponseDTO> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(ProductRestMapper.toResponseDTO(productUseCasePort.getProductByIdOrThrow(id)));
    }

    @GetMapping
    public ResponseEntity<List<ProductResponseDTO>> getAllProducts() {
        List<ProductResponseDTO> products = productUseCasePort.findAll().stream()
                .map(ProductRestMapper::toResponseDTO)
                .toList();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<ProductResponseDTO>> getProductsByCategory(@PathVariable Long categoryId) {
        List<ProductResponseDTO> products = productUseCasePort.findByCategory(categoryId).stream()
                .map(ProductRestMapper::toResponseDTO)
                .toList();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/seller/{sellerId}")
    public ResponseEntity<List<ProductResponseDTO>> getProductsBySeller(@PathVariable Long sellerId) {
        List<ProductResponseDTO> products = productUseCasePort.findBySeller(sellerId).stream()
                .map(ProductRestMapper::toResponseDTO)
                .toList();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/price-range")
    public ResponseEntity<List<ProductResponseDTO>> getProductsByPriceRange(@RequestParam BigDecimal min,
            @RequestParam BigDecimal max) {
        List<ProductResponseDTO> products = productUseCasePort.findByPriceRange(min, max).stream()
                .map(ProductRestMapper::toResponseDTO)
                .toList();
        return ResponseEntity.ok(products);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ProductResponseDTO> updateProduct(
            @AuthenticationPrincipal UserDetailsAdapter principal,
            @PathVariable Long id,
            @Valid @RequestBody ProductUpdateRequestDTO request) {

        requireOwnership(id, principal);
        Product updated = productUseCasePort.updateProduct(
                id,
                request.getName(),
                request.getDescription(),
                request.getPrice(),
                request.getType(),
                request.getCategoryId()
        );
        return ResponseEntity.ok(ProductRestMapper.toResponseDTO(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ProductResponseDTO> deleteProduct(
            @AuthenticationPrincipal UserDetailsAdapter principal,
            @PathVariable Long id) {

        requireOwnership(id, principal);
        Product deactivated = productUseCasePort.deactivateProduct(id);
        return ResponseEntity.ok(ProductRestMapper.toResponseDTO(deactivated));
    }

    /**
     * Resuelve el vendedor autenticado a partir del {@code userId} del token.
     * El {@code sellerId} ya no se acepta desde el cuerpo: era suplantable.
     */
    private Seller currentSeller(UserDetailsAdapter principal) {
        if (principal == null || principal.getUser() == null) {
            throw new AccessDeniedException("No authenticated seller found");
        }
        Long userId = principal.getUser().getId();
        return sellerRepositoryPort.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Seller", userId));
    }

    /**
     * Verifica que el producto pertenezca al vendedor autenticado antes de
     * permitir cualquier modificación. Evita que un vendedor altere o desactive
     * productos de otro (403 Forbidden).
     */
    private void requireOwnership(Long productId, UserDetailsAdapter principal) {
        Seller seller = currentSeller(principal);
        Product product = productUseCasePort.getProductByIdOrThrow(productId);
        if (!seller.getId().equals(product.getSellerId())) {
            throw new AccessDeniedException("Seller does not own product " + productId);
        }
    }
}
