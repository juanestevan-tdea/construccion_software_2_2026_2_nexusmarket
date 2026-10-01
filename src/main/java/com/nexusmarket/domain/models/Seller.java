package com.nexusmarket.domain.models;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Seller {

    private Long id;
    private Long userId;
    private String taxId;
    private String companyName;

    @Builder.Default
    private Boolean active = true;

    public void activate() {
        this.active = true;
    }

    public void deactivate() {
        this.active = false;
    }
}
