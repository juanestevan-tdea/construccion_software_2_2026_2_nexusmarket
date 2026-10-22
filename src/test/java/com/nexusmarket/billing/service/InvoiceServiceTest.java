package com.nexusmarket.billing.service;

import com.nexusmarket.adapters.useCases.InvoiceUseCaseImpl;
import com.nexusmarket.domain.exceptions.InvoiceAlreadyExistsException;
import com.nexusmarket.domain.exceptions.InvoiceNotPayableException;
import com.nexusmarket.domain.models.Invoice;
import com.nexusmarket.domain.models.Order;
import com.nexusmarket.domain.ports.in.supervisor.InvoiceUseCasePort;
import com.nexusmarket.domain.ports.out.InvoiceRepositoryPort;
import com.nexusmarket.domain.ports.out.OrderRepositoryPort;
import com.nexusmarket.domain.services.InvoiceGenerateService;
import com.nexusmarket.domain.services.InvoiceVoidService;
import com.nexusmarket.domain.valueobjects.OrderStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
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

    private InvoiceUseCasePort invoiceUseCase;

    private Order order;

    @BeforeEach
    void setUp() {
        order = new Order();
        order.setId(1L);
        order.setStatus(OrderStatus.PAID);
        order.setTotalAmount(BigDecimal.valueOf(250.00));

        InvoiceGenerateService generateService = new InvoiceGenerateService(invoiceRepositoryPort, orderRepositoryPort);
        InvoiceVoidService voidService = new InvoiceVoidService(invoiceRepositoryPort);
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
    void voidInvoice_Success_WhenInvoiceIsUnpaidAndActive() {
        Invoice invoice = Invoice.builder().id(5L).paid(false).active(true).build();
        when(invoiceRepositoryPort.findById(5L)).thenReturn(Optional.of(invoice));
        when(invoiceRepositoryPort.save(any(Invoice.class))).thenAnswer(i -> i.getArgument(0));

        Invoice response = invoiceUseCase.voidInvoice(5L);

        assertFalse(response.getActive());
        verify(invoiceRepositoryPort).save(invoice);
    }

    @Test
    void voidInvoice_ThrowsInvoiceNotPayableException_WhenPaid() {
        Invoice invoice = Invoice.builder().id(5L).paid(true).active(true).build();
        when(invoiceRepositoryPort.findById(5L)).thenReturn(Optional.of(invoice));

        assertThrows(InvoiceNotPayableException.class, () -> invoiceUseCase.voidInvoice(5L));
        verify(invoiceRepositoryPort, never()).save(any(Invoice.class));
    }

    @Test
    void voidInvoice_ThrowsInvoiceNotPayableException_WhenAlreadyInactive() {
        Invoice invoice = Invoice.builder().id(5L).paid(false).active(false).build();
        when(invoiceRepositoryPort.findById(5L)).thenReturn(Optional.of(invoice));

        assertThrows(InvoiceNotPayableException.class, () -> invoiceUseCase.voidInvoice(5L));
        verify(invoiceRepositoryPort, never()).save(any(Invoice.class));
    }
}


