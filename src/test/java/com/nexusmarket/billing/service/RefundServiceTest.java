package com.nexusmarket.billing.service;

import com.nexusmarket.adapters.useCases.RefundUseCaseImpl;
import com.nexusmarket.common.exception.RefundAmountExceededException;
import com.nexusmarket.common.exception.RefundNotAllowedException;
import com.nexusmarket.domain.models.Invoice;
import com.nexusmarket.domain.models.Refund;
import com.nexusmarket.domain.ports.in.RefundUseCasePort;
import com.nexusmarket.domain.ports.out.InvoiceRepositoryPort;
import com.nexusmarket.domain.ports.out.RefundRepositoryPort;
import com.nexusmarket.domain.services.RefundApproveService;
import com.nexusmarket.domain.services.RefundProcessService;
import com.nexusmarket.domain.valueobjects.RefundStatus;
import com.nexusmarket.logistics.domain.repository.ReturnRepository;
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
class RefundServiceTest {

    @Mock
    private RefundRepositoryPort refundRepositoryPort;
    @Mock
    private InvoiceRepositoryPort invoiceRepositoryPort;
    @Mock
    private ReturnRepository returnRepository;

    private RefundUseCasePort refundUseCase;

    private Invoice invoice;

    @BeforeEach
    void setUp() {
        invoice = Invoice.builder()
                .id(1L)
                .amount(BigDecimal.valueOf(100.00))
                .paid(true)
                .active(true)
                .build();

        RefundProcessService processService = new RefundProcessService(refundRepositoryPort, invoiceRepositoryPort, returnRepository);
        RefundApproveService approveService = new RefundApproveService(refundRepositoryPort);
        refundUseCase = new RefundUseCaseImpl(processService, approveService, refundRepositoryPort, invoiceRepositoryPort);
    }

    @Test
    void createRefund_Success() {
        when(invoiceRepositoryPort.findById(1L)).thenReturn(Optional.of(invoice));
        when(refundRepositoryPort.findByInvoiceId(1L)).thenReturn(Collections.emptyList());
        when(refundRepositoryPort.save(any(Refund.class))).thenAnswer(i -> {
            Refund r = i.getArgument(0);
            r.setId(10L);
            return r;
        });

        Refund response = refundUseCase.createRefund(1L, null, BigDecimal.valueOf(50.00));

        assertNotNull(response);
        assertEquals(BigDecimal.valueOf(50.00), response.getAmount());
        assertEquals(RefundStatus.PENDING, response.getStatus());
    }

    @Test
    void createRefund_ThrowsException_WhenInvoiceNotPaid() {
        invoice.setPaid(false);

        when(invoiceRepositoryPort.findById(1L)).thenReturn(Optional.of(invoice));

        assertThrows(RefundNotAllowedException.class, () -> refundUseCase.createRefund(1L, null, BigDecimal.valueOf(50.00)));
    }

    @Test
    void createRefund_ThrowsException_WhenAmountExceedsInvoice() {
        when(invoiceRepositoryPort.findById(1L)).thenReturn(Optional.of(invoice));

        assertThrows(RefundAmountExceededException.class, () -> refundUseCase.createRefund(1L, null, BigDecimal.valueOf(150.00)));
    }
}

