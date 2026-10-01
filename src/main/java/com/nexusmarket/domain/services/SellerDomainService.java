package com.nexusmarket.domain.services;

import com.nexusmarket.common.exception.BusinessRuleException;
import com.nexusmarket.common.exception.DuplicateResourceException;
import com.nexusmarket.common.exception.ResourceNotFoundException;
import com.nexusmarket.domain.models.Seller;
import com.nexusmarket.domain.models.User;
import com.nexusmarket.domain.ports.out.SellerRepositoryPort;
import com.nexusmarket.domain.ports.out.UserRepositoryPort;
import com.nexusmarket.domain.valueobjects.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SellerDomainService {

    private final SellerRepositoryPort sellerRepositoryPort;
    private final UserRepositoryPort userRepositoryPort;

    @Transactional
    public Seller createSeller(Long userId, String taxId, String companyName) {
        User user = userRepositoryPort.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        if (!user.getRole().equals(UserRole.SELLER)) {
            throw new BusinessRuleException("User is not a SELLER. Current role: " + user.getRole());
        }

        if (sellerRepositoryPort.findByUserId(userId).isPresent()) {
            throw new BusinessRuleException("User is already a registered seller");
        }

        if (sellerRepositoryPort.findByTaxId(taxId).isPresent()) {
            throw new DuplicateResourceException("Seller", "taxId", taxId);
        }

        Seller seller = Seller.builder()
                .userId(userId)
                .taxId(taxId)
                .companyName(companyName)
                .active(true)
                .build();

        return sellerRepositoryPort.save(seller);
    }

    @Transactional
    public Seller activateSeller(Long id) {
        Seller seller = sellerRepositoryPort.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Seller", id));
        seller.activate();
        return sellerRepositoryPort.save(seller);
    }

    @Transactional
    public Seller deactivateSeller(Long id) {
        Seller seller = sellerRepositoryPort.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Seller", id));
        seller.deactivate();
        return sellerRepositoryPort.save(seller);
    }

    @Transactional
    public Seller updateSeller(Long id, String companyName, String taxId) {
        Seller seller = sellerRepositoryPort.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Seller", id));

        if (companyName != null && !companyName.isEmpty()) {
            seller.setCompanyName(companyName);
        }

        if (taxId != null && !taxId.isEmpty()) {
            if (sellerRepositoryPort.findByTaxId(taxId).filter(s -> !s.getId().equals(id)).isPresent()) {
                throw new DuplicateResourceException("Seller", "taxId", taxId);
            }
            seller.setTaxId(taxId);
        }

        return sellerRepositoryPort.save(seller);
    }
}
