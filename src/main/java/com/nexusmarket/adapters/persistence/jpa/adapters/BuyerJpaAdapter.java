package com.nexusmarket.adapters.persistence.jpa.adapters;

import com.nexusmarket.adapters.persistence.jpa.entities.BuyerJpaEntity;
import com.nexusmarket.adapters.persistence.jpa.mappers.BuyerJpaMapper;
import com.nexusmarket.adapters.persistence.jpa.repositories.BuyerJpaRepository;
import com.nexusmarket.domain.models.Buyer;
import com.nexusmarket.domain.ports.out.BuyerRepositoryPort;
import com.nexusmarket.domain.valueobjects.BuyerCommercialStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class BuyerJpaAdapter implements BuyerRepositoryPort {

    private final BuyerJpaRepository buyerJpaRepository;
    private final BuyerJpaMapper buyerJpaMapper;

    @Override
    public Buyer save(Buyer buyer) {
        if (buyer == null) {
            return null;
        }
        BuyerJpaEntity entity = buyerJpaMapper.toEntity(buyer);
        BuyerJpaEntity saved = buyerJpaRepository.save(entity);
        return buyerJpaMapper.toDomain(saved);
    }

    @Override
    public Optional<Buyer> findById(Long id) {
        return buyerJpaRepository.findById(id)
                .map(buyerJpaMapper::toDomain);
    }

    @Override
    public boolean existsById(Long id) {
        return buyerJpaRepository.existsById(id);
    }

    @Override
    public List<Buyer> findAll() {
        return buyerJpaRepository.findAll().stream()
                .map(buyerJpaMapper::toDomain)
                .toList();
    }

    @Override
    public List<Buyer> findByCommercialStatus(BuyerCommercialStatus status) {
        return buyerJpaRepository.findByCommercialStatus(status).stream()
                .map(buyerJpaMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<Buyer> findByUserId(Long userId) {
        return buyerJpaRepository.findByUserId(userId)
                .map(buyerJpaMapper::toDomain);
    }
}
