package com.nexusmarket.domain.services;

import com.nexusmarket.domain.exceptions.ResourceNotFoundException;
import com.nexusmarket.domain.models.Buyer;
import com.nexusmarket.domain.ports.out.BuyerRepositoryPort;
import com.nexusmarket.domain.valueobjects.BuyerCommercialStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChangeBuyerCommercialStatusService {

    private final BuyerRepositoryPort buyerRepositoryPort;

    @Transactional
    public Buyer changeCommercialStatus(Long id, BuyerCommercialStatus newStatus) {
        Buyer buyer = buyerRepositoryPort.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Buyer", id));
        buyer.setCommercialStatus(newStatus);
        return buyerRepositoryPort.save(buyer);
    }
}
