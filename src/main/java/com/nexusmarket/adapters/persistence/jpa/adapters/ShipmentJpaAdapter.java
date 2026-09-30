package com.nexusmarket.adapters.persistence.jpa.adapters;

import com.nexusmarket.adapters.persistence.jpa.entities.ShipmentJpaEntity;
import com.nexusmarket.adapters.persistence.jpa.mappers.ShipmentJpaMapper;
import com.nexusmarket.adapters.persistence.jpa.repositories.ShipmentJpaRepository;
import com.nexusmarket.domain.models.Shipment;
import com.nexusmarket.domain.ports.out.ShipmentRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ShipmentJpaAdapter implements ShipmentRepositoryPort {

    private final ShipmentJpaRepository shipmentJpaRepository;
    private final ShipmentJpaMapper shipmentJpaMapper;

    @Override
    public Shipment save(Shipment shipment) {
        if (shipment == null) {
            return null;
        }
        ShipmentJpaEntity entity = shipmentJpaMapper.toEntity(shipment);
        ShipmentJpaEntity saved = shipmentJpaRepository.save(entity);
        return shipmentJpaMapper.toDomain(saved);
    }

    @Override
    public Optional<Shipment> findById(Long id) {
        return shipmentJpaRepository.findById(id)
                .map(shipmentJpaMapper::toDomain);
    }

    @Override
    public Optional<Shipment> findByTrackingNumber(String trackingNumber) {
        return shipmentJpaRepository.findByTrackingNumber(trackingNumber)
                .map(shipmentJpaMapper::toDomain);
    }

    @Override
    public Optional<Shipment> findByOrderId(Long orderId) {
        return shipmentJpaRepository.findByOrderId(orderId)
                .map(shipmentJpaMapper::toDomain);
    }

    @Override
    public boolean existsByOrderId(Long orderId) {
        return shipmentJpaRepository.existsByOrderId(orderId);
    }

    @Override
    public boolean existsByTrackingNumber(String trackingNumber) {
        return shipmentJpaRepository.existsByTrackingNumber(trackingNumber);
    }

    @Override
    public List<Shipment> findAll() {
        return shipmentJpaRepository.findAll().stream()
                .map(shipmentJpaMapper::toDomain)
                .toList();
    }
}
