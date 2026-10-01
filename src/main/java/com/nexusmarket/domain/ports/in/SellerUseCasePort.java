package com.nexusmarket.domain.ports.in;

import com.nexusmarket.domain.models.Seller;

import java.util.List;
import java.util.Optional;

public interface SellerUseCasePort {

    Seller createSeller(Long userId, String taxId, String companyName);

    Seller getSellerByIdOrThrow(Long id);

    Seller getSellerByTaxIdOrThrow(String taxId);

    Optional<Seller> findById(Long id);

    Optional<Seller> findByUserId(Long userId);

    Optional<Seller> findByTaxId(String taxId);

    List<Seller> findAll();

    List<Seller> findByActive(Boolean active);

    Seller activateSeller(Long id);

    Seller deactivateSeller(Long id);

    Seller updateSeller(Long id, String companyName, String taxId);
}
