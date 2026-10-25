package com.nexusmarket.domain.services;

import com.nexusmarket.domain.exceptions.ResourceNotFoundException;
import com.nexusmarket.domain.models.Buyer;
import com.nexusmarket.domain.ports.out.BuyerRepositoryPort;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AddBuyerAddressService {

    private final BuyerRepositoryPort buyerRepositoryPort;

    public Buyer addAdditionalAddress(Long id, String address) {
        Buyer buyer = buyerRepositoryPort.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Buyer", id));
        buyer.addAdditionalAddress(address);
        return buyerRepositoryPort.save(buyer);
    }
}
