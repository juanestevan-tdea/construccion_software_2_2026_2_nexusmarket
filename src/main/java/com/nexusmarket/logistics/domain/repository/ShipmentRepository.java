package com.nexusmarket.logistics.domain.repository;

import com.nexusmarket.logistics.domain.model.Shipment;
import com.nexusmarket.orders.domain.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ShipmentRepository extends JpaRepository<Shipment, Long> {
    Optional<Shipment> findByTrackingNumber(String trackingNumber);
    Optional<Shipment> findByOrder(Order order);
    boolean existsByOrder(Order order);
    boolean existsByTrackingNumber(String trackingNumber);
}