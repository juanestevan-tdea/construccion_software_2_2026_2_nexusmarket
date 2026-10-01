package com.nexusmarket.adapters.persistence.jpa.mappers;

import com.nexusmarket.adapters.persistence.jpa.entities.WarehouseJpaEntity;
import com.nexusmarket.domain.models.Warehouse;
import org.springframework.stereotype.Component;

@Component
public class WarehouseJpaMapper {

    public Warehouse toDomain(WarehouseJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        return Warehouse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .location(entity.getLocation())
                .type(entity.getType())
                .capacity(entity.getCapacity())
                .build();
    }

    public WarehouseJpaEntity toEntity(Warehouse domain) {
        if (domain == null) {
            return null;
        }
        return WarehouseJpaEntity.builder()
                .id(domain.getId())
                .name(domain.getName())
                .location(domain.getLocation())
                .type(domain.getType())
                .capacity(domain.getCapacity())
                .build();
    }
}
