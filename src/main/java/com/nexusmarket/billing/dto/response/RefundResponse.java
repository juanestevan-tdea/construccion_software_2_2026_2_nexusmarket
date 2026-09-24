package com.nexusmarket.billing.dto.response;

import com.nexusmarket.billing.domain.model.Refund;
import com.nexusmarket.billing.domain.model.RefundStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefundResponse {

    private Long id;
    private Long invoiceId;
    private Long returnRequestId;
    private BigDecimal amount;
    private RefundStatus status;

    public static RefundResponse fromEntity(Refund refund) {
        if (refund == null) {
            return null;
        }
        return RefundResponse.builder()
                .id(refund.getId())
                .invoiceId(refund.getInvoice() != null ? refund.getInvoice().getId() : null)
                .returnRequestId(refund.getReturnRequest() != null ? refund.getReturnRequest().getId() : null)
                .amount(refund.getAmount())
                .status(refund.getStatus())
                .build();
    }
}
