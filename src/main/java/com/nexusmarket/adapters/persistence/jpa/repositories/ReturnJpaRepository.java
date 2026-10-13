package com.nexusmarket.adapters.persistence.jpa.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.nexusmarket.adapters.persistence.jpa.entities.OrderJpaEntity;
import com.nexusmarket.adapters.persistence.jpa.entities.ReturnJpaEntity;

public interface ReturnJpaRepository extends JpaRepository<ReturnJpaEntity, Long> {

    List<ReturnJpaEntity> findByOrder(OrderJpaEntity order);

    Optional<ReturnJpaEntity> findFirstByOrderOrderByRequestedAtDesc(OrderJpaEntity order);

    @Query("SELECT r FROM ReturnJpaEntity r WHERE r.order.id = :orderId")
    List<ReturnJpaEntity> findByOrderId(@Param("orderId") Long orderId);

    @Query("SELECT r FROM ReturnJpaEntity r WHERE r.order.id = :orderId ORDER BY r.requestedAt DESC LIMIT 1")
    Optional<ReturnJpaEntity> findFirstByOrderIdOrderByRequestedAtDesc(@Param("orderId") Long orderId);
}
