package com.nexusmarket.billing.dto.response;

import com.nexusmarket.billing.domain.model.Invoice;
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
public class InvoiceResponse {

    private Long id;
    private Long orderId;
    private String invoiceNumber;
    private LocalDateTime issueDate;
    private BigDecimal amount;
    private String pdfUrl;
    private Boolean active;
    private Boolean paid;

    public static InvoiceResponse fromEntity(Invoice invoice) {
        if (invoice == null) {
            return null;
        }
        return InvoiceResponse.builder()
                .id(invoice.getId())
                .orderId(invoice.getOrder() != null ? invoice.getOrder().getId() : null)
                .invoiceNumber(invoice.getInvoiceNumber())
                .issueDate(invoice.getIssueDate())
                .amount(invoice.getAmount())
                .pdfUrl(invoice.getPdfUrl())
                .active(invoice.getActive())
                .paid(invoice.getPaid())
                .build();
    }
}
