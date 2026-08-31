package com.nexusmarket.users.service;

import com.nexusmarket.users.domain.model.Seller;
import com.nexusmarket.users.domain.model.User;
import com.nexusmarket.users.domain.repository.SellerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SellerService {

    private final SellerRepository sellerRepository;
    private final UserService userService;

    // Crear un vendedor a partir de un usuario existente (solo Admin)
    @Transactional
    public Seller createSeller(Long userId, String taxId, String companyName) {
        User user = userService.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        // Validar que el usuario tenga rol SELLER
        if (!user.getRole().equals(UserRole.SELLER)) {
            throw new IllegalArgumentException("User is not a SELLER. Current role: " + user.getRole());
        }

        // Validar que el taxId no exista
        if (sellerRepository.findByTaxId(taxId).isPresent()) {
            throw new IllegalArgumentException("Tax ID already exists: " + taxId);
        }

        Seller seller = Seller.builder()
                .user(user)
                .taxId(taxId)
                .companyName(companyName)
                .active(true)
                .build();

        return sellerRepository.save(seller);
    }

    // Buscar vendedor por ID
    public Optional<Seller> findById(Long id) {
        return sellerRepository.findById(id);
    }

    // Buscar vendedor por taxId (NIT/RUT)
    public Optional<Seller> findByTaxId(String taxId) {
        return sellerRepository.findByTaxId(taxId);
    }

    // Listar todos los vendedores
    public List<Seller> findAll() {
        return sellerRepository.findAll();
    }

    // Listar vendedores activos/inactivos
    public List<Seller> findByActive(Boolean active) {
        return sellerRepository.findByActive(active);
    }

    // Activar vendedor
    @Transactional
    public Seller activateSeller(Long sellerId) {
        Seller seller = sellerRepository.findById(sellerId)
                .orElseThrow(() -> new IllegalArgumentException("Seller not found: " + sellerId));
        seller.activate();
        return sellerRepository.save(seller);
    }

    // Desactivar vendedor
    @Transactional
    public Seller deactivateSeller(Long sellerId) {
        Seller seller = sellerRepository.findById(sellerId)
                .orElseThrow(() -> new IllegalArgumentException("Seller not found: " + sellerId));
        seller.deactivate();
        return sellerRepository.save(seller);
    }

    // Actualizar información del vendedor
    @Transactional
    public Seller updateSeller(Long sellerId, String companyName, String taxId) {
        Seller seller = sellerRepository.findById(sellerId)
                .orElseThrow(() -> new IllegalArgumentException("Seller not found: " + sellerId));

        if (companyName != null && !companyName.isEmpty()) {
            seller.setCompanyName(companyName);
        }

        if (taxId != null && !taxId.isEmpty()) {
            // Validar que el nuevo taxId no esté en uso
            if (sellerRepository.findByTaxId(taxId).isPresent()) {
                throw new IllegalArgumentException("Tax ID already exists: " + taxId);
            }
            seller.setTaxId(taxId);
        }

        return sellerRepository.save(seller);
    }
}