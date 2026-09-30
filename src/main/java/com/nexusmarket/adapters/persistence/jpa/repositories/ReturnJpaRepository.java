package com.nexusmarket.adapters.persistence.jpa.repositories;

import com.nexusmarket.adapters.persistence.jpa.entities.ReturnJpaEntity;
import com.nexusmarket.orders.domain.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReturnJpaRepository extends JpaRepository<ReturnJpaEntity, Long> {

    List<ReturnJpaEntity> findByOrder(Order order);

    Optional<ReturnJpaEntity> findFirstByOrderOrderByRequestedAtDesc(Order order);

    @Query("SELECT r FROM ReturnJpaEntity r WHERE r.order.id = :orderId")
    List<ReturnJpaEntity> findByOrderId(@Param("orderId") Long orderId);

    @Query("SELECT r FROM ReturnJpaEntity r WHERE r.order.id = :orderId ORDER BY r.requestedAt DESC LIMIT 1")
    Optional<ReturnJpaEntity> findFirstByOrderIdOrderByRequestedAtDesc(@Param("orderId") Long orderId);
}
