package com.nexusmarket.billing.service;

import com.nexusmarket.billing.domain.model.Invoice;
import com.nexusmarket.billing.domain.model.Refund;
import com.nexusmarket.billing.domain.model.RefundStatus;
import com.nexusmarket.billing.domain.repository.InvoiceRepository;
import com.nexusmarket.billing.domain.repository.RefundRepository;
import com.nexusmarket.billing.dto.request.InvoiceCreateRequest;
import com.nexusmarket.billing.dto.response.InvoiceResponse;
import com.nexusmarket.common.exception.InvoiceAlreadyExistsException;
import com.nexusmarket.common.exception.InvoiceNotPayableException;
import com.nexusmarket.common.exception.RefundNotAllowedException;
import com.nexusmarket.orders.domain.model.Order;
import com.nexusmarket.orders.domain.model.OrderStatus;
import com.nexusmarket.orders.domain.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InvoiceServiceTest {

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private RefundRepository refundRepository;

    @InjectMocks
    private InvoiceService invoiceService;

    private Order order;

    @BeforeEach
    void setUp() {
        order = new Order();
        order.setId(1L);
        order.setStatus(OrderStatus.PAID);
        order.setTotalAmount(BigDecimal.valueOf(250.00));
    }

    @Test
    void generateInvoice_Success() {
        InvoiceCreateRequest request = InvoiceCreateRequest.builder()
                .orderId(1L)
                .pdfUrl("http://pdf.com/1")
                .build();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(invoiceRepository.existsByOrderAndActiveTrue(order)).thenReturn(false);
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(i -> {
            Invoice inv = i.getArgument(0);
            inv.setId(10L);
            return inv;
        });

        InvoiceResponse response = invoiceService.generateInvoice(request);

        assertNotNull(response);
        assertEquals(BigDecimal.valueOf(250.00), response.getAmount());
        assertTrue(response.getActive());
    }

    @Test
    void generateInvoice_ThrowsInvoiceNotPayableException_WhenOrderInCart() {
        order.setStatus(OrderStatus.CART);
        InvoiceCreateRequest request = InvoiceCreateRequest.builder().orderId(1L).build();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThrows(InvoiceNotPayableException.class, () -> invoiceService.generateInvoice(request));
    }

    @Test
    void generateInvoice_ThrowsInvoiceAlreadyExistsException_WhenInvoiceAlreadyExists() {
        InvoiceCreateRequest request = InvoiceCreateRequest.builder().orderId(1L).build();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(invoiceRepository.existsByOrderAndActiveTrue(order)).thenReturn(true);

        assertThrows(InvoiceAlreadyExistsException.class, () -> invoiceService.generateInvoice(request));
    }

    @Test
    void voidInvoice_ThrowsRefundNotAllowedException_WhenPaidAndNoApprovedRefund() {
        Invoice invoice = Invoice.builder().id(5L).paid(true).active(true).build();
        when(invoiceRepository.findById(5L)).thenReturn(Optional.of(invoice));
        when(refundRepository.findByInvoiceAndStatus(invoice, RefundStatus.APPROVED)).thenReturn(Collections.emptyList());

        assertThrows(RefundNotAllowedException.class, () -> invoiceService.voidInvoice(5L));
    }
}
