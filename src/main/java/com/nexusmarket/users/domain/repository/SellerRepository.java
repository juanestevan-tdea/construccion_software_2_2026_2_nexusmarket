package com.nexusmarket.users.domain.repository;


import com.nexusmarket.users.domain.model.Seller;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SellerRepository extends JpaRepository<Seller, Long> {
    Optional<Seller> findByTaxId(String taxId);
    List<Seller> findByActive(Boolean active);
}