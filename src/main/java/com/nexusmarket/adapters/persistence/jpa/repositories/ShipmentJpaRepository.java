package com.nexusmarket.adapters.persistence.jpa.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.nexusmarket.adapters.persistence.jpa.entities.OrderJpaEntity;
import com.nexusmarket.adapters.persistence.jpa.entities.ShipmentJpaEntity;

@Repository
public interface ShipmentJpaRepository extends JpaRepository<ShipmentJpaEntity, Long> {

    Optional<ShipmentJpaEntity> findByTrackingNumber(String trackingNumber);

    Optional<ShipmentJpaEntity> findByOrder(OrderJpaEntity order);

    boolean existsByOrder(OrderJpaEntity order);

    boolean existsByTrackingNumber(String trackingNumber);

    @Query("SELECT s FROM ShipmentJpaEntity s WHERE s.order.id = :orderId")
    Optional<ShipmentJpaEntity> findByOrderId(@Param("orderId") Long orderId);

    @Query("SELECT COUNT(s) > 0 FROM ShipmentJpaEntity s WHERE s.order.id = :orderId")
    boolean existsByOrderId(@Param("orderId") Long orderId);
}
