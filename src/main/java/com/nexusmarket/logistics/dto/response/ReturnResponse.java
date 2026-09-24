package com.nexusmarket.logistics.dto.response;

import com.nexusmarket.logistics.domain.model.Return;
import com.nexusmarket.logistics.domain.model.ReturnStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReturnResponse {

    private Long id;
    private Long orderId;
    private String reason;
    private ReturnStatus status;
    private LocalDateTime requestedAt;
    private LocalDateTime resolvedAt;

    public static ReturnResponse fromEntity(Return returnEntity) {
        if (returnEntity == null) {
            return null;
        }
        return ReturnResponse.builder()
                .id(returnEntity.getId())
                .orderId(returnEntity.getOrder() != null ? returnEntity.getOrder().getId() : null)
                .reason(returnEntity.getReason())
                .status(returnEntity.getStatus())
                .requestedAt(returnEntity.getRequestedAt())
                .resolvedAt(returnEntity.getResolvedAt())
                .build();
    }
}
