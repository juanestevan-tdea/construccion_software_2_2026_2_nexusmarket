package com.nexusmarket.infrastructure.config;

import com.nexusmarket.domain.ports.out.InvoiceRepositoryPort;
import com.nexusmarket.domain.ports.out.OrderRepositoryPort;
import com.nexusmarket.domain.ports.out.RefundRepositoryPort;
import com.nexusmarket.domain.ports.out.ReturnRepositoryPort;
import com.nexusmarket.domain.ports.out.ShipmentRepositoryPort;
import com.nexusmarket.domain.ports.out.WarehouseRepositoryPort;
import com.nexusmarket.domain.services.InvoiceGenerateService;
import com.nexusmarket.domain.services.InvoiceVoidService;
import com.nexusmarket.domain.services.RefundApproveService;
import com.nexusmarket.domain.services.RefundProcessService;
import com.nexusmarket.domain.services.ReturnProcessService;
import com.nexusmarket.domain.services.ShipmentCreateService;
import com.nexusmarket.domain.services.ShipmentStatusService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Registro de los servicios de dominio de facturación, reembolsos,
 * devoluciones y envíos.
 *
 * <p>Los servicios son POJO puros: no llevan {@code @Service} ni
 * {@code @Transactional}. Las transacciones viven en las fachadas de
 * {@code adapters/useCases}.</p>
 */
@Configuration
public class BillingServiceConfig {

    @Bean
    public InvoiceGenerateService invoiceGenerateService(InvoiceRepositoryPort invoiceRepositoryPort,
            OrderRepositoryPort orderRepositoryPort) {
        return new InvoiceGenerateService(invoiceRepositoryPort, orderRepositoryPort);
    }

    @Bean
    public InvoiceVoidService invoiceVoidService(InvoiceRepositoryPort invoiceRepositoryPort) {
        return new InvoiceVoidService(invoiceRepositoryPort);
    }

    @Bean
    public RefundProcessService refundProcessService(RefundRepositoryPort refundRepositoryPort,
            InvoiceRepositoryPort invoiceRepositoryPort,
            ReturnRepositoryPort returnRepositoryPort) {
        return new RefundProcessService(refundRepositoryPort, invoiceRepositoryPort, returnRepositoryPort);
    }

    @Bean
    public RefundApproveService refundApproveService(RefundRepositoryPort refundRepositoryPort) {
        return new RefundApproveService(refundRepositoryPort);
    }

    @Bean
    public ReturnProcessService returnProcessService(ReturnRepositoryPort returnRepositoryPort,
            OrderRepositoryPort orderRepositoryPort) {
        return new ReturnProcessService(returnRepositoryPort, orderRepositoryPort);
    }

    @Bean
    public ShipmentCreateService shipmentCreateService(
            ShipmentRepositoryPort shipmentRepositoryPort,
            OrderRepositoryPort orderRepositoryPort,
            WarehouseRepositoryPort warehouseRepositoryPort) {
        return new ShipmentCreateService(shipmentRepositoryPort, orderRepositoryPort,
                warehouseRepositoryPort);
    }

    @Bean
    public ShipmentStatusService shipmentStatusService(ShipmentRepositoryPort shipmentRepositoryPort) {
        return new ShipmentStatusService(shipmentRepositoryPort);
    }
}
