package com.nexusmarket.domain.services;

import com.nexusmarket.common.exception.BusinessRuleException;
import com.nexusmarket.common.exception.ResourceNotFoundException;
import com.nexusmarket.domain.models.Buyer;
import com.nexusmarket.domain.models.User;
import com.nexusmarket.domain.ports.out.BuyerRepositoryPort;
import com.nexusmarket.domain.ports.out.UserRepositoryPort;
import com.nexusmarket.domain.valueobjects.BuyerCommercialStatus;
import com.nexusmarket.domain.valueobjects.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BuyerDomainService {

    private final BuyerRepositoryPort buyerRepositoryPort;
    private final UserRepositoryPort userRepositoryPort;

    @Transactional
    public Buyer createBuyer(Long userId, String primaryAddress) {
        User user = userRepositoryPort.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        if (!user.getRole().equals(UserRole.BUYER)) {
            throw new BusinessRuleException("User is not a BUYER. Current role: " + user.getRole());
        }

        if (buyerRepositoryPort.findByUserId(userId).isPresent()) {
            throw new BusinessRuleException("User is already a registered buyer");
        }

        Buyer buyer = Buyer.builder()
                .userId(userId)
                .primaryAddress(primaryAddress)
                .commercialStatus(BuyerCommercialStatus.ACTIVE)
                .build();

        return buyerRepositoryPort.save(buyer);
    }

    @Transactional
    public Buyer addAdditionalAddress(Long id, String address) {
        Buyer buyer = buyerRepositoryPort.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Buyer", id));
        buyer.addAdditionalAddress(address);
        return buyerRepositoryPort.save(buyer);
    }

    @Transactional
    public Buyer changeCommercialStatus(Long id, BuyerCommercialStatus newStatus) {
        Buyer buyer = buyerRepositoryPort.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Buyer", id));
        buyer.setCommercialStatus(newStatus);
        return buyerRepositoryPort.save(buyer);
    }
}
