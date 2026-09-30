package com.nexusmarket.adapters.rest.dtos.responses;

import com.nexusmarket.domain.valueobjects.ReturnStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReturnResponseDTO {

    private Long id;
    private Long orderId;
    private String reason;
    private ReturnStatus status;
    private LocalDateTime requestedAt;
    private LocalDateTime resolvedAt;
}
