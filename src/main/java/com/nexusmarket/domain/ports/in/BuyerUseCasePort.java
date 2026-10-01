package com.nexusmarket.domain.ports.in;

import com.nexusmarket.domain.models.Buyer;
import com.nexusmarket.domain.valueobjects.BuyerCommercialStatus;

import java.util.List;
import java.util.Optional;

public interface BuyerUseCasePort {

    Buyer createBuyer(Long userId, String primaryAddress);

    Buyer getBuyerByIdOrThrow(Long id);

    Optional<Buyer> findById(Long id);

    Optional<Buyer> findByUserId(Long userId);

    List<Buyer> findAll();

    List<Buyer> findByCommercialStatus(BuyerCommercialStatus status);

    Buyer addAdditionalAddress(Long id, String address);

    Buyer changeCommercialStatus(Long id, BuyerCommercialStatus newStatus);
}
