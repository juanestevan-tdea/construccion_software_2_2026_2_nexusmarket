package com.nexusmarket.domain.services;

import com.nexusmarket.domain.exceptions.ResourceNotFoundException;
import com.nexusmarket.domain.models.Buyer;
import com.nexusmarket.domain.ports.out.BuyerRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AddBuyerAddressService {

    private final BuyerRepositoryPort buyerRepositoryPort;

    @Transactional
    public Buyer addAdditionalAddress(Long id, String address) {
        Buyer buyer = buyerRepositoryPort.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Buyer", id));
        buyer.addAdditionalAddress(address);
        return buyerRepositoryPort.save(buyer);
    }
}
