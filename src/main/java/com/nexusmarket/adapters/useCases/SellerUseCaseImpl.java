package com.nexusmarket.adapters.useCases;

import com.nexusmarket.common.exception.ResourceNotFoundException;
import com.nexusmarket.domain.models.Seller;
import com.nexusmarket.domain.ports.in.SellerUseCasePort;
import com.nexusmarket.domain.ports.out.SellerRepositoryPort;
import com.nexusmarket.domain.services.SellerDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SellerUseCaseImpl implements SellerUseCasePort {

    private final SellerDomainService sellerDomainService;
    private final SellerRepositoryPort sellerRepositoryPort;

    @Override
    public Seller createSeller(Long userId, String taxId, String companyName) {
        return sellerDomainService.createSeller(userId, taxId, companyName);
    }

    @Override
    public Seller getSellerByIdOrThrow(Long id) {
        return sellerRepositoryPort.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Seller", id));
    }

    @Override
    public Seller getSellerByTaxIdOrThrow(String taxId) {
        return sellerRepositoryPort.findByTaxId(taxId)
                .orElseThrow(() -> new ResourceNotFoundException("Seller with taxId '" + taxId + "' was not found"));
    }

    @Override
    public Optional<Seller> findById(Long id) {
        return sellerRepositoryPort.findById(id);
    }

    @Override
    public Optional<Seller> findByUserId(Long userId) {
        return sellerRepositoryPort.findByUserId(userId);
    }

    @Override
    public Optional<Seller> findByTaxId(String taxId) {
        return sellerRepositoryPort.findByTaxId(taxId);
    }

    @Override
    public List<Seller> findAll() {
        return sellerRepositoryPort.findAll();
    }

    @Override
    public List<Seller> findByActive(Boolean active) {
        return sellerRepositoryPort.findByActive(active);
    }

    @Override
    public Seller activateSeller(Long id) {
        return sellerDomainService.activateSeller(id);
    }

    @Override
    public Seller deactivateSeller(Long id) {
        return sellerDomainService.deactivateSeller(id);
    }

    @Override
    public Seller updateSeller(Long id, String companyName, String taxId) {
        return sellerDomainService.updateSeller(id, companyName, taxId);
    }
}
