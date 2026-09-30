package com.nexusmarket.adapters.rest.mappers;

import com.nexusmarket.adapters.rest.dtos.responses.RefundResponseDTO;
import com.nexusmarket.domain.models.Refund;
import lombok.experimental.UtilityClass;

@UtilityClass
public class RefundRestMapper {

    public RefundResponseDTO toResponseDTO(Refund domain) {
        if (domain == null) {
            return null;
        }
        return RefundResponseDTO.builder()
                .id(domain.getId())
                .invoiceId(domain.getInvoiceId())
                .returnRequestId(domain.getReturnRequestId())
                .amount(domain.getAmount())
                .status(domain.getStatus())
                .build();
    }
}
