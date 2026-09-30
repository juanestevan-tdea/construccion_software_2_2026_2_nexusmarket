package com.nexusmarket.adapters.persistence.jpa.repositories;

import com.nexusmarket.adapters.persistence.jpa.entities.ShipmentJpaEntity;
import com.nexusmarket.orders.domain.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ShipmentJpaRepository extends JpaRepository<ShipmentJpaEntity, Long> {

    Optional<ShipmentJpaEntity> findByTrackingNumber(String trackingNumber);

    Optional<ShipmentJpaEntity> findByOrder(Order order);

    boolean existsByOrder(Order order);

    boolean existsByTrackingNumber(String trackingNumber);

    @Query("SELECT s FROM ShipmentJpaEntity s WHERE s.order.id = :orderId")
    Optional<ShipmentJpaEntity> findByOrderId(@Param("orderId") Long orderId);

    @Query("SELECT COUNT(s) > 0 FROM ShipmentJpaEntity s WHERE s.order.id = :orderId")
    boolean existsByOrderId(@Param("orderId") Long orderId);
}
