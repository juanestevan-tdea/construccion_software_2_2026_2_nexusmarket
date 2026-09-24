package com.nexusmarket.billing.service;

import com.nexusmarket.billing.domain.model.Invoice;
import com.nexusmarket.billing.domain.model.Refund;
import com.nexusmarket.billing.domain.model.RefundStatus;
import com.nexusmarket.billing.domain.repository.InvoiceRepository;
import com.nexusmarket.billing.domain.repository.RefundRepository;
import com.nexusmarket.billing.dto.request.RefundCreateRequest;
import com.nexusmarket.billing.dto.response.RefundResponse;
import com.nexusmarket.common.exception.RefundAmountExceededException;
import com.nexusmarket.common.exception.RefundNotAllowedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefundServiceTest {

    @Mock
    private RefundRepository refundRepository;
    @Mock
    private InvoiceRepository invoiceRepository;

    @InjectMocks
    private RefundService refundService;

    private Invoice invoice;

    @BeforeEach
    void setUp() {
        invoice = Invoice.builder()
                .id(1L)
                .amount(BigDecimal.valueOf(100.00))
                .paid(true)
                .active(true)
                .build();
    }

    @Test
    void createRefund_Success() {
        RefundCreateRequest request = RefundCreateRequest.builder()
                .invoiceId(1L)
                .amount(BigDecimal.valueOf(50.00))
                .build();

        when(invoiceRepository.findById(1L)).thenReturn(Optional.of(invoice));
        when(refundRepository.findByInvoice(invoice)).thenReturn(Collections.emptyList());
        when(refundRepository.save(any(Refund.class))).thenAnswer(i -> {
            Refund r = i.getArgument(0);
            r.setId(10L);
            return r;
        });

        RefundResponse response = refundService.createRefund(request);

        assertNotNull(response);
        assertEquals(BigDecimal.valueOf(50.00), response.getAmount());
        assertEquals(RefundStatus.PENDING, response.getStatus());
    }

    @Test
    void createRefund_ThrowsException_WhenInvoiceNotPaid() {
        invoice.setPaid(false);
        RefundCreateRequest request = RefundCreateRequest.builder()
                .invoiceId(1L)
                .amount(BigDecimal.valueOf(50.00))
                .build();

        when(invoiceRepository.findById(1L)).thenReturn(Optional.of(invoice));

        assertThrows(RefundNotAllowedException.class, () -> refundService.createRefund(request));
    }

    @Test
    void createRefund_ThrowsException_WhenAmountExceedsInvoice() {
        RefundCreateRequest request = RefundCreateRequest.builder()
                .invoiceId(1L)
                .amount(BigDecimal.valueOf(150.00))
                .build();

        when(invoiceRepository.findById(1L)).thenReturn(Optional.of(invoice));

        assertThrows(RefundAmountExceededException.class, () -> refundService.createRefund(request));
    }
}
