package com.nexusmarket.adapters.rest.mappers;

import com.nexusmarket.adapters.rest.dtos.responses.InvoiceResponseDTO;
import com.nexusmarket.domain.models.Invoice;
import lombok.experimental.UtilityClass;

@UtilityClass
public class InvoiceRestMapper {

    public InvoiceResponseDTO toResponseDTO(Invoice domain) {
        if (domain == null) {
            return null;
        }
        return InvoiceResponseDTO.builder()
                .id(domain.getId())
                .orderId(domain.getOrderId())
                .invoiceNumber(domain.getInvoiceNumber())
                .issueDate(domain.getIssueDate())
                .amount(domain.getAmount())
                .pdfUrl(domain.getPdfUrl())
                .active(domain.getActive())
                .paid(domain.getPaid())
                .build();
    }
}
