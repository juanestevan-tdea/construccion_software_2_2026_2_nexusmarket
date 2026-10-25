package com.nexusmarket.infrastructure.config;

import com.nexusmarket.domain.ports.out.BuyerRepositoryPort;
import com.nexusmarket.domain.ports.out.CategoryRepositoryPort;
import com.nexusmarket.domain.ports.out.InventoryRepositoryPort;
import com.nexusmarket.domain.ports.out.OrderRepositoryPort;
import com.nexusmarket.domain.ports.out.ProductRepositoryPort;
import com.nexusmarket.domain.ports.out.SellerRepositoryPort;
import com.nexusmarket.domain.ports.out.UserRepositoryPort;
import com.nexusmarket.domain.ports.out.WarehouseRepositoryPort;
import com.nexusmarket.domain.services.CategoryDomainService;
import com.nexusmarket.domain.services.InventoryConsultService;
import com.nexusmarket.domain.services.InventoryManagementService;
import com.nexusmarket.domain.services.OrderCalculationService;
import com.nexusmarket.domain.services.OrderCreateService;
import com.nexusmarket.domain.services.OrderLifecycleService;
import com.nexusmarket.domain.services.ProductDomainService;
import com.nexusmarket.domain.services.SellerDomainService;
import com.nexusmarket.domain.services.WarehouseDomainService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Registro de los servicios de dominio de catálogo, inventario, almacenes,
 * vendedores y pedidos.
 *
 * <p>Los servicios son POJO puros: no llevan {@code @Service} ni
 * {@code @Transactional}. Las transacciones viven en las fachadas de
 * {@code adapters/useCases}.</p>
 */
@Configuration
public class CommerceServiceConfig {

    @Bean
    public ProductDomainService productDomainService(ProductRepositoryPort productRepositoryPort,
            CategoryRepositoryPort categoryRepositoryPort,
            SellerRepositoryPort sellerRepositoryPort,
            UserRepositoryPort userRepositoryPort,
            InventoryRepositoryPort inventoryRepositoryPort) {
        return new ProductDomainService(productRepositoryPort, categoryRepositoryPort,
                sellerRepositoryPort, userRepositoryPort, inventoryRepositoryPort);
    }

    @Bean
    public CategoryDomainService categoryDomainService(CategoryRepositoryPort categoryRepositoryPort,
            ProductRepositoryPort productRepositoryPort) {
        return new CategoryDomainService(categoryRepositoryPort, productRepositoryPort);
    }

    @Bean
    public InventoryManagementService inventoryManagementService(
            InventoryRepositoryPort inventoryRepositoryPort,
            ProductRepositoryPort productRepositoryPort,
            WarehouseRepositoryPort warehouseRepositoryPort) {
        return new InventoryManagementService(inventoryRepositoryPort, productRepositoryPort,
                warehouseRepositoryPort);
    }

    @Bean
    public InventoryConsultService inventoryConsultService(
            InventoryRepositoryPort inventoryRepositoryPort,
            ProductRepositoryPort productRepositoryPort,
            WarehouseRepositoryPort warehouseRepositoryPort) {
        return new InventoryConsultService(inventoryRepositoryPort, productRepositoryPort,
                warehouseRepositoryPort);
    }

    @Bean
    public WarehouseDomainService warehouseDomainService(
            WarehouseRepositoryPort warehouseRepositoryPort,
            InventoryRepositoryPort inventoryRepositoryPort) {
        return new WarehouseDomainService(warehouseRepositoryPort, inventoryRepositoryPort);
    }

    @Bean
    public SellerDomainService sellerDomainService(SellerRepositoryPort sellerRepositoryPort,
            UserRepositoryPort userRepositoryPort) {
        return new SellerDomainService(sellerRepositoryPort, userRepositoryPort);
    }

    @Bean
    public OrderCreateService orderCreateService(OrderRepositoryPort orderRepositoryPort,
            BuyerRepositoryPort buyerRepositoryPort,
            UserRepositoryPort userRepositoryPort) {
        return new OrderCreateService(orderRepositoryPort, buyerRepositoryPort, userRepositoryPort);
    }

    @Bean
    public OrderCalculationService orderCalculationService(OrderRepositoryPort orderRepositoryPort,
            ProductRepositoryPort productRepositoryPort,
            InventoryRepositoryPort inventoryRepositoryPort) {
        return new OrderCalculationService(orderRepositoryPort, productRepositoryPort,
                inventoryRepositoryPort);
    }

    @Bean
    public OrderLifecycleService orderLifecycleService(OrderRepositoryPort orderRepositoryPort) {
        return new OrderLifecycleService(orderRepositoryPort);
    }
}
