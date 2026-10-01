package com.nexusmarket.billing.service;

import com.nexusmarket.adapters.useCases.InvoiceUseCaseImpl;
import com.nexusmarket.common.exception.InvoiceAlreadyExistsException;
import com.nexusmarket.common.exception.InvoiceNotPayableException;
import com.nexusmarket.common.exception.RefundNotAllowedException;
import com.nexusmarket.domain.models.Invoice;
import com.nexusmarket.domain.models.Order;
import com.nexusmarket.domain.ports.in.InvoiceUseCasePort;
import com.nexusmarket.domain.ports.out.InvoiceRepositoryPort;
import com.nexusmarket.domain.ports.out.OrderRepositoryPort;
import com.nexusmarket.domain.ports.out.RefundRepositoryPort;
import com.nexusmarket.domain.services.InvoiceGenerateService;
import com.nexusmarket.domain.services.InvoiceVoidService;
import com.nexusmarket.domain.valueobjects.OrderStatus;
import com.nexusmarket.domain.valueobjects.RefundStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InvoiceServiceTest {

    @Mock
    private InvoiceRepositoryPort invoiceRepositoryPort;

    @Mock
    private OrderRepositoryPort orderRepositoryPort;

    @Mock
    private RefundRepositoryPort refundRepositoryPort;

    private InvoiceUseCasePort invoiceUseCase;

    private Order order;

    @BeforeEach
    void setUp() {
        order = new Order();
        order.setId(1L);
        order.setStatus(OrderStatus.PAID);
        order.setTotalAmount(BigDecimal.valueOf(250.00));

        InvoiceGenerateService generateService = new InvoiceGenerateService(invoiceRepositoryPort, orderRepositoryPort);
        InvoiceVoidService voidService = new InvoiceVoidService(invoiceRepositoryPort, refundRepositoryPort);
        invoiceUseCase = new InvoiceUseCaseImpl(generateService, voidService, invoiceRepositoryPort, orderRepositoryPort);
    }

    @Test
    void generateInvoice_Success() {
        when(orderRepositoryPort.findById(1L)).thenReturn(Optional.of(order));
        when(invoiceRepositoryPort.existsByOrderIdAndActiveTrue(1L)).thenReturn(false);
        when(invoiceRepositoryPort.save(any(Invoice.class))).thenAnswer(i -> {
            Invoice inv = i.getArgument(0);
            inv.setId(10L);
            return inv;
        });

        Invoice response = invoiceUseCase.generateInvoice(1L, "http://pdf.com/1");

        assertNotNull(response);
        assertEquals(BigDecimal.valueOf(250.00), response.getAmount());
        assertTrue(response.getActive());
    }

    @Test
    void generateInvoice_ThrowsInvoiceNotPayableException_WhenOrderInCart() {
        order.setStatus(OrderStatus.CART);

        when(orderRepositoryPort.findById(1L)).thenReturn(Optional.of(order));

        assertThrows(InvoiceNotPayableException.class, () -> invoiceUseCase.generateInvoice(1L, null));
    }

    @Test
    void generateInvoice_ThrowsInvoiceAlreadyExistsException_WhenInvoiceAlreadyExists() {
        when(orderRepositoryPort.findById(1L)).thenReturn(Optional.of(order));
        when(invoiceRepositoryPort.existsByOrderIdAndActiveTrue(1L)).thenReturn(true);

        assertThrows(InvoiceAlreadyExistsException.class, () -> invoiceUseCase.generateInvoice(1L, null));
    }

    @Test
    void voidInvoice_ThrowsRefundNotAllowedException_WhenPaidAndNoApprovedRefund() {
        Invoice invoice = Invoice.builder().id(5L).paid(true).active(true).build();
        when(invoiceRepositoryPort.findById(5L)).thenReturn(Optional.of(invoice));
        when(refundRepositoryPort.findByInvoiceIdAndStatus(5L, RefundStatus.APPROVED)).thenReturn(Collections.emptyList());

        assertThrows(RefundNotAllowedException.class, () -> invoiceUseCase.voidInvoice(5L));
    }
}

