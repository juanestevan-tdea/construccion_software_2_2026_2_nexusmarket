package com.nexusmarket.users.dto;

import com.nexusmarket.users.domain.model.Seller;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SellerResponse {

    private Long id;
    private UserResponse user;
    private String taxId;
    private String companyName;
    private Boolean active;

    public static SellerResponse fromEntity(Seller seller) {
        if (seller == null) {
            return null;
        }
        return SellerResponse.builder()
                .id(seller.getId())
                .user(UserResponse.fromEntity(seller.getUser()))
                .taxId(seller.getTaxId())
                .companyName(seller.getCompanyName())
                .active(seller.getActive())
                .build();
    }
}
