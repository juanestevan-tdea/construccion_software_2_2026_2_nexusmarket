package com.nexusmarket.adapters.useCases;

import com.nexusmarket.domain.exceptions.ResourceNotFoundException;
import com.nexusmarket.domain.models.Buyer;
import com.nexusmarket.domain.ports.in.buyer.BuyerUseCasePort;
import com.nexusmarket.domain.ports.out.BuyerRepositoryPort;
import com.nexusmarket.domain.services.AddBuyerAddressService;
import com.nexusmarket.domain.services.ChangeBuyerCommercialStatusService;
import com.nexusmarket.domain.services.CreateBuyerService;
import com.nexusmarket.domain.valueobjects.BuyerCommercialStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class BuyerUseCaseImpl implements BuyerUseCasePort {

    private final CreateBuyerService createBuyerService;
    private final AddBuyerAddressService addBuyerAddressService;
    private final ChangeBuyerCommercialStatusService changeBuyerCommercialStatusService;
    private final BuyerRepositoryPort buyerRepositoryPort;

    @Override
    public Buyer createBuyer(Long userId, String primaryAddress) {
        return createBuyerService.createBuyer(userId, primaryAddress);
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
        return addBuyerAddressService.addAdditionalAddress(id, address);
    }

    @Override
    public Buyer changeCommercialStatus(Long id, BuyerCommercialStatus newStatus) {
        return changeBuyerCommercialStatusService.changeCommercialStatus(id, newStatus);
    }
}

