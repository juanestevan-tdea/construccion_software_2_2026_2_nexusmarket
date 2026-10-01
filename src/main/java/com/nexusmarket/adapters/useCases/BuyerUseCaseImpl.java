package com.nexusmarket.adapters.useCases;

import com.nexusmarket.common.exception.ResourceNotFoundException;
import com.nexusmarket.domain.models.Buyer;
import com.nexusmarket.domain.ports.in.BuyerUseCasePort;
import com.nexusmarket.domain.ports.out.BuyerRepositoryPort;
import com.nexusmarket.domain.services.BuyerDomainService;
import com.nexusmarket.domain.valueobjects.BuyerCommercialStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BuyerUseCaseImpl implements BuyerUseCasePort {

    private final BuyerDomainService buyerDomainService;
    private final BuyerRepositoryPort buyerRepositoryPort;

    @Override
    public Buyer createBuyer(Long userId, String primaryAddress) {
        return buyerDomainService.createBuyer(userId, primaryAddress);
    }

    @Override
    public Buyer getBuyerByIdOrThrow(Long id) {
        return buyerRepositoryPort.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Buyer", id));
    }

    @Override
    public Optional<Buyer> findById(Long id) {
        return buyerRepositoryPort.findById(id);
    }

    @Override
    public Optional<Buyer> findByUserId(Long userId) {
        return buyerRepositoryPort.findByUserId(userId);
    }

    @Override
    public List<Buyer> findAll() {
        return buyerRepositoryPort.findAll();
    }

    @Override
    public List<Buyer> findByCommercialStatus(BuyerCommercialStatus status) {
        return buyerRepositoryPort.findByCommercialStatus(status);
    }

    @Override
    public Buyer addAdditionalAddress(Long id, String address) {
        return buyerDomainService.addAdditionalAddress(id, address);
    }

    @Override
    public Buyer changeCommercialStatus(Long id, BuyerCommercialStatus newStatus) {
        return buyerDomainService.changeCommercialStatus(id, newStatus);
    }
}
