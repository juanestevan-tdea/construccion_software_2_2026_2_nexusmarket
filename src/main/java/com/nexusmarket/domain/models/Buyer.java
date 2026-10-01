package com.nexusmarket.domain.models;

import com.nexusmarket.domain.valueobjects.BuyerCommercialStatus;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Buyer {

    private Long id;
    private Long userId;
    private String primaryAddress;

    @Builder.Default
    private List<String> additionalAddresses = new ArrayList<>();

    private BuyerCommercialStatus commercialStatus;

    public void addAdditionalAddress(String address) {
        if (this.additionalAddresses == null) {
            this.additionalAddresses = new ArrayList<>();
        }
        this.additionalAddresses.add(address);
    }

    public void removeAdditionalAddress(String address) {
        if (this.additionalAddresses != null) {
            this.additionalAddresses.remove(address);
        }
    }
}
