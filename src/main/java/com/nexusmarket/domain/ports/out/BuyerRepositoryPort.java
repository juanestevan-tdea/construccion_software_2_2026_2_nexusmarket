package com.nexusmarket.domain.ports.out;

import com.nexusmarket.domain.models.Buyer;
import com.nexusmarket.domain.valueobjects.BuyerCommercialStatus;

import java.util.List;
import java.util.Optional;

public interface BuyerRepositoryPort {

    Buyer save(Buyer buyer);

    Optional<Buyer> findById(Long id);

    boolean existsById(Long id);

    List<Buyer> findAll();

    List<Buyer> findByCommercialStatus(BuyerCommercialStatus status);

    Optional<Buyer> findByUserId(Long userId);
}
