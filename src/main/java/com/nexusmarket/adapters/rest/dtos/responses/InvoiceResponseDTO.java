package com.nexusmarket.adapters.rest.dtos.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceResponseDTO {

    private Long id;
    private Long orderId;
    private String invoiceNumber;
    private LocalDateTime issueDate;
    private BigDecimal amount;
    private String pdfUrl;
    private Boolean active;
    private Boolean paid;
}
