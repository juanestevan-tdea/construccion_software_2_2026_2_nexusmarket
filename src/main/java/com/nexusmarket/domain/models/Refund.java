package com.nexusmarket.domain.models;

import com.nexusmarket.domain.valueobjects.RefundStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Refund {

    private Long id;
    private Long invoiceId;
    private Long returnRequestId;
    private BigDecimal amount;
    private RefundStatus status;
}
