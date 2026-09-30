package com.nexusmarket.adapters.persistence.jpa.adapters;

import com.nexusmarket.adapters.persistence.jpa.entities.RefundJpaEntity;
import com.nexusmarket.adapters.persistence.jpa.mappers.RefundJpaMapper;
import com.nexusmarket.adapters.persistence.jpa.repositories.RefundJpaRepository;
import com.nexusmarket.domain.models.Refund;
import com.nexusmarket.domain.ports.out.RefundRepositoryPort;
import com.nexusmarket.domain.valueobjects.RefundStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class RefundJpaAdapter implements RefundRepositoryPort {

    private final RefundJpaRepository refundJpaRepository;
    private final RefundJpaMapper refundJpaMapper;

    @Override
    public Refund save(Refund refund) {
        if (refund == null) {
            return null;
        }
        RefundJpaEntity entity = refundJpaMapper.toEntity(refund);
        RefundJpaEntity saved = refundJpaRepository.save(entity);
        return refundJpaMapper.toDomain(saved);
    }

    @Override
    public Optional<Refund> findById(Long id) {
        return refundJpaRepository.findById(id)
                .map(refundJpaMapper::toDomain);
    }

    @Override
    public List<Refund> findByInvoiceId(Long invoiceId) {
        return refundJpaRepository.findByInvoiceId(invoiceId).stream()
                .map(refundJpaMapper::toDomain)
                .toList();
    }

    @Override
    public List<Refund> findByInvoiceIdAndStatus(Long invoiceId, RefundStatus status) {
        return refundJpaRepository.findByInvoiceIdAndStatus(invoiceId, status).stream()
                .map(refundJpaMapper::toDomain)
                .toList();
    }

    @Override
    public List<Refund> findAll() {
        return refundJpaRepository.findAll().stream()
                .map(refundJpaMapper::toDomain)
                .toList();
    }
}
