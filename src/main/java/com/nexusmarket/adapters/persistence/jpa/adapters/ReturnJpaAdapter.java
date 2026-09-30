package com.nexusmarket.adapters.persistence.jpa.adapters;

import com.nexusmarket.adapters.persistence.jpa.entities.ReturnJpaEntity;
import com.nexusmarket.adapters.persistence.jpa.mappers.ReturnJpaMapper;
import com.nexusmarket.adapters.persistence.jpa.repositories.ReturnJpaRepository;
import com.nexusmarket.domain.models.Return;
import com.nexusmarket.domain.ports.out.ReturnRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ReturnJpaAdapter implements ReturnRepositoryPort {

    private final ReturnJpaRepository returnJpaRepository;
    private final ReturnJpaMapper returnJpaMapper;

    @Override
    public Return save(Return returnEntity) {
        if (returnEntity == null) {
            return null;
        }
        ReturnJpaEntity entity = returnJpaMapper.toEntity(returnEntity);
        ReturnJpaEntity saved = returnJpaRepository.save(entity);
        return returnJpaMapper.toDomain(saved);
    }

    @Override
    public Optional<Return> findById(Long id) {
        return returnJpaRepository.findById(id)
                .map(returnJpaMapper::toDomain);
    }

    @Override
    public boolean existsById(Long id) {
        return returnJpaRepository.existsById(id);
    }

    @Override
    public List<Return> findByOrderId(Long orderId) {
        return returnJpaRepository.findByOrderId(orderId).stream()
                .map(returnJpaMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<Return> findFirstByOrderIdOrderByRequestedAtDesc(Long orderId) {
        return returnJpaRepository.findFirstByOrderIdOrderByRequestedAtDesc(orderId)
                .map(returnJpaMapper::toDomain);
    }

    @Override
    public List<Return> findAll() {
        return returnJpaRepository.findAll().stream()
                .map(returnJpaMapper::toDomain)
                .toList();
    }
}
