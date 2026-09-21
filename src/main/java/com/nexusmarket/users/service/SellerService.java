package com.nexusmarket.users.service;

import com.nexusmarket.exception.BusinessRuleException;
import com.nexusmarket.exception.DuplicateResourceException;
import com.nexusmarket.exception.ResourceNotFoundException;
import com.nexusmarket.users.domain.model.Seller;
import com.nexusmarket.users.domain.model.User;
import com.nexusmarket.users.domain.model.UserRole;
import com.nexusmarket.users.domain.repository.SellerRepository;
import com.nexusmarket.users.dto.SellerCreateRequest;
import com.nexusmarket.users.dto.SellerResponse;
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

    // Crear un vendedor a partir de un DTO
    @Transactional
    public SellerResponse createSeller(SellerCreateRequest request) {
        Seller seller = createSeller(request.getUserId(), request.getTaxId(), request.getCompanyName());
        return SellerResponse.fromEntity(seller);
    }

    // Crear un vendedor a partir de un usuario existente (solo Admin)
    @Transactional
    public Seller createSeller(Long userId, String taxId, String companyName) {
        User user = userService.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        // Validar que el usuario tenga rol SELLER
        if (!user.getRole().equals(UserRole.SELLER)) {
            throw new BusinessRuleException("User is not a SELLER. Current role: " + user.getRole());
        }

        // Validar que el usuario no tenga ya un perfil de vendedor asociado
        if (sellerRepository.findByUser(user).isPresent()) {
            throw new BusinessRuleException("User is already a registered seller");
        }

        // Validar que el taxId no exista
        if (sellerRepository.findByTaxId(taxId).isPresent()) {
            throw new DuplicateResourceException("Seller", "taxId", taxId);
        }

        Seller seller = Seller.builder()
                .user(user)
                .taxId(taxId)
                .companyName(companyName)
                .active(true)
                .build();

        return sellerRepository.save(seller);
    }

    // Buscar vendedor por ID o lanzar excepción
    public Seller getSellerByIdOrThrow(Long id) {
        return sellerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Seller", id));
    }

    // Buscar vendedor por taxId o lanzar excepción
    public Seller getSellerByTaxIdOrThrow(String taxId) {
        return sellerRepository.findByTaxId(taxId)
                .orElseThrow(() -> new ResourceNotFoundException("Seller with taxId '" + taxId + "' was not found"));
    }

    // Buscar vendedor por ID
    public Optional<Seller> findById(Long id) {
        return sellerRepository.findById(id);
    }

    // Buscar vendedor por usuario
    public Optional<Seller> findByUser(User user) {
        return sellerRepository.findByUser(user);
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
    public Seller activateSeller(Long id) {
        Seller seller = sellerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Seller", id));
        seller.activate();
        return sellerRepository.save(seller);
    }

    // Desactivar vendedor
    @Transactional
    public Seller deactivateSeller(Long id) {
        Seller seller = sellerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Seller", id));
        seller.deactivate();
        return sellerRepository.save(seller);
    }

    // Actualizar información del vendedor
    @Transactional
    public Seller updateSeller(Long id, String companyName, String taxId) {
        Seller seller = sellerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Seller", id));

        if (companyName != null && !companyName.isEmpty()) {
            seller.setCompanyName(companyName);
        }

        if (taxId != null && !taxId.isEmpty()) {
            // Validar que el nuevo taxId no esté en uso por otro vendedor
            if (sellerRepository.findByTaxId(taxId).filter(s -> !s.getId().equals(id)).isPresent()) {
                throw new DuplicateResourceException("Seller", "taxId", taxId);
            }
            seller.setTaxId(taxId);
        }

        return sellerRepository.save(seller);
    }
}