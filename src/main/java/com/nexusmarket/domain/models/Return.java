package com.nexusmarket.domain.models;

import com.nexusmarket.domain.valueobjects.ReturnStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Return {

    private Long id;
    private Long orderId;
    private String reason;
    private ReturnStatus status;
    @Builder.Default
    private LocalDateTime requestedAt = LocalDateTime.now();
    private LocalDateTime resolvedAt;
}
