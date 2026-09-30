package com.nexusmarket.domain.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Invoice {

    private Long id;
    private Long orderId;
    private String invoiceNumber;
    private LocalDateTime issueDate;
    private BigDecimal amount;
    private String pdfUrl;
    @Builder.Default
    private Boolean active = true;
    @Builder.Default
    private Boolean paid = true;
}
