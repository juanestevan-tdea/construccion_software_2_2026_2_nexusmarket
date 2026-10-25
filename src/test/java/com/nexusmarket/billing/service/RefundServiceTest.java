package com.nexusmarket.billing.service;

import com.nexusmarket.adapters.useCases.RefundUseCaseImpl;
import com.nexusmarket.domain.exceptions.InvalidRefundAmountException;
import com.nexusmarket.domain.exceptions.InvalidStatusTransitionException;
import com.nexusmarket.domain.exceptions.InvoiceNotRefundableException;
import com.nexusmarket.domain.exceptions.RefundAmountExceededException;
import com.nexusmarket.domain.exceptions.RefundNotAllowedException;
import com.nexusmarket.domain.models.Invoice;
import com.nexusmarket.domain.models.Refund;
import com.nexusmarket.domain.models.Return;
import com.nexusmarket.domain.ports.in.supervisor.RefundUseCasePort;
import com.nexusmarket.domain.ports.out.InvoiceRepositoryPort;
import com.nexusmarket.domain.ports.out.RefundRepositoryPort;
import com.nexusmarket.domain.ports.out.ReturnRepositoryPort;
import com.nexusmarket.domain.services.RefundApproveService;
import com.nexusmarket.domain.services.RefundProcessService;
import com.nexusmarket.domain.valueobjects.RefundStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
class RefundServiceTest {

    @Mock
    private RefundRepositoryPort refundRepositoryPort;
    @Mock
    private InvoiceRepositoryPort invoiceRepositoryPort;
    @Mock
    private ReturnRepositoryPort returnRepositoryPort;

    private RefundUseCasePort refundUseCase;

    private Invoice invoice;

    @BeforeEach
    void setUp() {
        invoice = Invoice.builder()
                .id(1L)
                .orderId(100L)
                .amount(BigDecimal.valueOf(100.00))
                .paid(true)
                .active(true)
                .build();

        RefundProcessService processService = new RefundProcessService(refundRepositoryPort, invoiceRepositoryPort, returnRepositoryPort);
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

        assertThrows(InvoiceNotRefundableException.class, () -> refundUseCase.createRefund(1L, null, BigDecimal.valueOf(50.00)));
    }

    @Test
    void createRefund_ThrowsException_WhenInvoiceIsInactive() {
        invoice.setActive(false);

        when(invoiceRepositoryPort.findById(1L)).thenReturn(Optional.of(invoice));

        assertThrows(InvoiceNotRefundableException.class, () -> refundUseCase.createRefund(1L, null, BigDecimal.valueOf(50.00)));
        verify(refundRepositoryPort, never()).save(any(Refund.class));
    }

    @Test
    void createRefund_ThrowsException_WhenAmountExceedsInvoice() {
        when(invoiceRepositoryPort.findById(1L)).thenReturn(Optional.of(invoice));

        assertThrows(RefundAmountExceededException.class, () -> refundUseCase.createRefund(1L, null, BigDecimal.valueOf(150.00)));
    }

    @Test
    void createRefund_ThrowsException_WhenCumulativeRefundsExceedInvoice() {
        Refund previous = Refund.builder()
                .id(20L)
                .invoiceId(1L)
                .amount(BigDecimal.valueOf(80.00))
                .status(RefundStatus.PENDING)
                .build();

        when(invoiceRepositoryPort.findById(1L)).thenReturn(Optional.of(invoice));
        when(refundRepositoryPort.findByInvoiceId(1L)).thenReturn(List.of(previous));

        assertThrows(RefundAmountExceededException.class,
                () -> refundUseCase.createRefund(1L, null, BigDecimal.valueOf(30.00)));
    }

    @Test
    void createRefund_ThrowsException_WhenAmountIsNotPositive() {
        assertThrows(InvalidRefundAmountException.class,
                () -> refundUseCase.createRefund(1L, null, BigDecimal.ZERO));
        verify(refundRepositoryPort, never()).save(any(Refund.class));
    }

    @Test
    void createRefund_ThrowsException_WhenReturnBelongsToAnotherOrder() {
        Return otherOrderReturn = Return.builder().id(7L).orderId(999L).build();

        when(invoiceRepositoryPort.findById(1L)).thenReturn(Optional.of(invoice));
        when(refundRepositoryPort.findByInvoiceId(1L)).thenReturn(Collections.emptyList());
        when(returnRepositoryPort.findById(7L)).thenReturn(Optional.of(otherOrderReturn));

        assertThrows(RefundNotAllowedException.class,
                () -> refundUseCase.createRefund(1L, 7L, BigDecimal.valueOf(10.00)));
        verify(refundRepositoryPort, never()).save(any(Refund.class));
    }

    @Test
    void createRefund_ThrowsException_WhenReturnNotFound() {
        when(invoiceRepositoryPort.findById(1L)).thenReturn(Optional.of(invoice));
        when(refundRepositoryPort.findByInvoiceId(1L)).thenReturn(Collections.emptyList());
        when(returnRepositoryPort.findById(7L)).thenReturn(Optional.empty());

        assertThrows(com.nexusmarket.domain.exceptions.ResourceNotFoundException.class,
                () -> refundUseCase.createRefund(1L, 7L, BigDecimal.valueOf(10.00)));
        verify(refundRepositoryPort, never()).save(any(Refund.class));
    }

    @Test
    void createRefund_Success_WhenReturnBelongsToInvoiceOrder() {
        Return sameOrderReturn = Return.builder().id(8L).orderId(100L).build();

        when(invoiceRepositoryPort.findById(1L)).thenReturn(Optional.of(invoice));
        when(refundRepositoryPort.findByInvoiceId(1L)).thenReturn(Collections.emptyList());
        when(returnRepositoryPort.findById(8L)).thenReturn(Optional.of(sameOrderReturn));
        when(refundRepositoryPort.save(any(Refund.class))).thenAnswer(i -> i.getArgument(0));

        Refund result = refundUseCase.createRefund(1L, 8L, BigDecimal.valueOf(10.00));

        assertEquals(8L, result.getReturnRequestId());
        assertEquals(RefundStatus.PENDING, result.getStatus());
    }

    @Test
    void approveRefund_ChangesOnlyPendingRefund() {
        Refund pending = Refund.builder()
                .id(11L)
                .invoiceId(1L)
                .amount(BigDecimal.TEN)
                .status(RefundStatus.PENDING)
                .build();
        when(refundRepositoryPort.findById(11L)).thenReturn(Optional.of(pending));
        when(refundRepositoryPort.save(any(Refund.class))).thenAnswer(i -> i.getArgument(0));

        Refund approved = refundUseCase.approveRefund(11L);

        assertEquals(RefundStatus.APPROVED, approved.getStatus());
    }

    @Test
    void approveRefund_ThrowsInvalidStatusTransition_WhenRefundWasRejected() {
        Refund rejected = Refund.builder().id(12L).status(RefundStatus.REJECTED).build();
        when(refundRepositoryPort.findById(12L)).thenReturn(Optional.of(rejected));

        InvalidStatusTransitionException exception = assertThrows(
                InvalidStatusTransitionException.class,
                () -> refundUseCase.approveRefund(12L));

        assertEquals("REJECTED", exception.getCurrentStatus());
        assertEquals("APPROVED", exception.getTargetStatus());
        verify(refundRepositoryPort, never()).save(any(Refund.class));
    }

    @Test
    void rejectRefund_ThrowsInvalidStatusTransition_WhenRefundWasApproved() {
        Refund approved = Refund.builder().id(13L).status(RefundStatus.APPROVED).build();
        when(refundRepositoryPort.findById(13L)).thenReturn(Optional.of(approved));

        InvalidStatusTransitionException exception = assertThrows(
                InvalidStatusTransitionException.class,
                () -> refundUseCase.rejectRefund(13L));

        assertEquals("APPROVED", exception.getCurrentStatus());
        assertEquals("REJECTED", exception.getTargetStatus());
        verify(refundRepositoryPort, never()).save(any(Refund.class));
    }
}


