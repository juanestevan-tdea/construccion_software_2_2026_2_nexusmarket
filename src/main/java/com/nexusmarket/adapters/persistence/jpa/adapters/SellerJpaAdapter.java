package com.nexusmarket.adapters.persistence.jpa.adapters;

import com.nexusmarket.adapters.persistence.jpa.entities.SellerJpaEntity;
import com.nexusmarket.adapters.persistence.jpa.mappers.SellerJpaMapper;
import com.nexusmarket.adapters.persistence.jpa.repositories.SellerJpaRepository;
import com.nexusmarket.domain.models.Seller;
import com.nexusmarket.domain.ports.out.SellerRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class SellerJpaAdapter implements SellerRepositoryPort {

    private final SellerJpaRepository sellerJpaRepository;
    private final SellerJpaMapper sellerJpaMapper;

    @Override
    public Seller save(Seller seller) {
        if (seller == null) {
            return null;
        }
        SellerJpaEntity entity = sellerJpaMapper.toEntity(seller);
        SellerJpaEntity saved = sellerJpaRepository.save(entity);
        return sellerJpaMapper.toDomain(saved);
    }

    @Override
    public Optional<Seller> findById(Long id) {
        return sellerJpaRepository.findById(id)
                .map(sellerJpaMapper::toDomain);
    }

    @Override
    public Optional<Seller> findByTaxId(String taxId) {
        return sellerJpaRepository.findByTaxId(taxId)
                .map(sellerJpaMapper::toDomain);
    }

    @Override
    public List<Seller> findAll() {
        return sellerJpaRepository.findAll().stream()
                .map(sellerJpaMapper::toDomain)
                .toList();
    }

    @Override
    public List<Seller> findByActive(Boolean active) {
        return sellerJpaRepository.findByActive(active).stream()
                .map(sellerJpaMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<Seller> findByUserId(Long userId) {
        return sellerJpaRepository.findByUserId(userId)
                .map(sellerJpaMapper::toDomain);
    }
}
