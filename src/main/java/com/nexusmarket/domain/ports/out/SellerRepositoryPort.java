package com.nexusmarket.domain.ports.out;

import com.nexusmarket.domain.models.Seller;

import java.util.List;
import java.util.Optional;

public interface SellerRepositoryPort {

    Seller save(Seller seller);

    Optional<Seller> findById(Long id);

    Optional<Seller> findByTaxId(String taxId);

    List<Seller> findAll();

    List<Seller> findByActive(Boolean active);

    Optional<Seller> findByUserId(Long userId);
}
