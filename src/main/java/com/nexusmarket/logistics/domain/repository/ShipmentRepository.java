package com.nexusmarket.logistics.domain.repository;

import com.nexusmarket.logistics.domain.model.Shipment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShipmentRepository extends JpaRepository<Shipment, Long> {
}