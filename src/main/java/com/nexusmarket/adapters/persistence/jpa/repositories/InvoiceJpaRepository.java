package com.nexusmarket.adapters.persistence.jpa.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.nexusmarket.adapters.persistence.jpa.entities.InvoiceJpaEntity;
import com.nexusmarket.adapters.persistence.jpa.entities.OrderJpaEntity;

@Repository
public interface InvoiceJpaRepository extends JpaRepository<InvoiceJpaEntity, Long> {

    Optional<InvoiceJpaEntity> findByOrder(OrderJpaEntity order);

    Optional<InvoiceJpaEntity> findByInvoiceNumber(String invoiceNumber);

    boolean existsByOrderAndActiveTrue(OrderJpaEntity order);

    List<InvoiceJpaEntity> findByActive(Boolean active);

    @Query("SELECT i FROM InvoiceJpaEntity i WHERE i.order.id = :orderId")
    Optional<InvoiceJpaEntity> findByOrderId(@Param("orderId") Long orderId);

    @Query("SELECT COUNT(i) > 0 FROM InvoiceJpaEntity i WHERE i.order.id = :orderId AND i.active = true")
    boolean existsByOrderIdAndActiveTrue(@Param("orderId") Long orderId);
}
