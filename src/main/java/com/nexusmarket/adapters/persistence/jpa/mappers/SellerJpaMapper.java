package com.nexusmarket.adapters.persistence.jpa.mappers;

import com.nexusmarket.adapters.persistence.jpa.entities.SellerJpaEntity;
import com.nexusmarket.adapters.persistence.jpa.entities.UserJpaEntity;
import com.nexusmarket.adapters.persistence.jpa.repositories.UserJpaRepository;
import com.nexusmarket.domain.exceptions.ResourceNotFoundException;
import com.nexusmarket.domain.models.Seller;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SellerJpaMapper {

    private final UserJpaRepository userJpaRepository;

    public Seller toDomain(SellerJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        return Seller.builder()
                .id(entity.getId())
                .userId(entity.getUser() != null ? entity.getUser().getId() : null)
                .taxId(entity.getTaxId())
                .companyName(entity.getCompanyName())
                .active(entity.getActive())
                .build();
    }

    public SellerJpaEntity toEntity(Seller domain) {
        if (domain == null) {
            return null;
        }
        UserJpaEntity user = null;
        if (domain.getUserId() != null) {
            user = userJpaRepository.findById(domain.getUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("User", domain.getUserId()));
        }

        return SellerJpaEntity.builder()
                .id(domain.getId())
                .user(user)
                .taxId(domain.getTaxId())
                .companyName(domain.getCompanyName())
                .active(domain.getActive())
                .build();
    }
}

