package com.nexusmarket.domain.ports.in;

import com.nexusmarket.domain.models.Warehouse;
import com.nexusmarket.domain.valueobjects.WarehouseType;

import java.util.List;

public interface WarehouseUseCasePort {

    Warehouse createWarehouse(String name, String location, WarehouseType type, Integer capacity);

    Warehouse getWarehouseByIdOrThrow(Long id);

    List<Warehouse> findAll();

    List<Warehouse> findByType(WarehouseType type);

    Warehouse updateWarehouse(Long id, String name, String location, WarehouseType type, Integer capacity);
}
