package com.nexusmarket.adapters.useCases;

import com.nexusmarket.common.exception.ResourceNotFoundException;
import com.nexusmarket.domain.models.Warehouse;
import com.nexusmarket.domain.ports.in.WarehouseUseCasePort;
import com.nexusmarket.domain.ports.out.WarehouseRepositoryPort;
import com.nexusmarket.domain.services.WarehouseDomainService;
import com.nexusmarket.domain.valueobjects.WarehouseType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WarehouseUseCaseImpl implements WarehouseUseCasePort {

    private final WarehouseDomainService warehouseDomainService;
    private final WarehouseRepositoryPort warehouseRepositoryPort;

    @Override
    public Warehouse createWarehouse(String name, String location, WarehouseType type, Integer capacity) {
        return warehouseDomainService.createWarehouse(name, location, type, capacity);
    }

    @Override
    public Warehouse getWarehouseByIdOrThrow(Long id) {
        return warehouseRepositoryPort.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse", id));
    }

    @Override
    public List<Warehouse> findAll() {
        return warehouseRepositoryPort.findAll();
    }

    @Override
    public List<Warehouse> findByType(WarehouseType type) {
        return warehouseRepositoryPort.findByType(type);
    }

    @Override
    public Warehouse updateWarehouse(Long id, String name, String location, WarehouseType type, Integer capacity) {
        return warehouseDomainService.updateWarehouse(id, name, location, type, capacity);
    }
}
