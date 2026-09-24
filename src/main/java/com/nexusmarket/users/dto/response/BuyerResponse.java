package com.nexusmarket.users.dto.response;

import com.nexusmarket.users.domain.model.Buyer;
import com.nexusmarket.users.domain.model.BuyerCommercialStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BuyerResponse {

    private Long id;
    private UserResponse user;
    private String primaryAddress;
    private List<String> additionalAddresses;
    private BuyerCommercialStatus commercialStatus;

    public static BuyerResponse fromEntity(Buyer buyer) {
        if (buyer == null) {
            return null;
        }
        return BuyerResponse.builder()
                .id(buyer.getId())
                .user(UserResponse.fromEntity(buyer.getUser()))
                .primaryAddress(buyer.getPrimaryAddress())
                .additionalAddresses(buyer.getAdditionalAddresses())
                .commercialStatus(buyer.getCommercialStatus())
                .build();
    }
}
