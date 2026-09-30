package com.nexusmarket.adapters.persistence.jpa.adapters;

import com.nexusmarket.adapters.persistence.jpa.entities.InvoiceJpaEntity;
import com.nexusmarket.adapters.persistence.jpa.mappers.InvoiceJpaMapper;
import com.nexusmarket.adapters.persistence.jpa.repositories.InvoiceJpaRepository;
import com.nexusmarket.domain.models.Invoice;
import com.nexusmarket.domain.ports.out.InvoiceRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class InvoiceJpaAdapter implements InvoiceRepositoryPort {

    private final InvoiceJpaRepository invoiceJpaRepository;
    private final InvoiceJpaMapper invoiceJpaMapper;

    @Override
    public Invoice save(Invoice invoice) {
        if (invoice == null) {
            return null;
        }
        InvoiceJpaEntity entity = invoiceJpaMapper.toEntity(invoice);
        InvoiceJpaEntity saved = invoiceJpaRepository.save(entity);
        return invoiceJpaMapper.toDomain(saved);
    }

    @Override
    public Optional<Invoice> findById(Long id) {
        return invoiceJpaRepository.findById(id)
                .map(invoiceJpaMapper::toDomain);
    }

    @Override
    public Optional<Invoice> findByOrderId(Long orderId) {
        return invoiceJpaRepository.findByOrderId(orderId)
                .map(invoiceJpaMapper::toDomain);
    }

    @Override
    public Optional<Invoice> findByInvoiceNumber(String invoiceNumber) {
        return invoiceJpaRepository.findByInvoiceNumber(invoiceNumber)
                .map(invoiceJpaMapper::toDomain);
    }

    @Override
    public boolean existsByOrderIdAndActiveTrue(Long orderId) {
        return invoiceJpaRepository.existsByOrderIdAndActiveTrue(orderId);
    }

    @Override
    public List<Invoice> findByActive(Boolean active) {
        return invoiceJpaRepository.findByActive(active).stream()
                .map(invoiceJpaMapper::toDomain)
                .toList();
    }

    @Override
    public List<Invoice> findAll() {
        return invoiceJpaRepository.findAll().stream()
                .map(invoiceJpaMapper::toDomain)
                .toList();
    }
}
