package com.nexusmarket.adapters.persistence.jpa.mappers;

import com.nexusmarket.adapters.persistence.jpa.entities.BuyerJpaEntity;
import com.nexusmarket.adapters.persistence.jpa.entities.UserJpaEntity;
import com.nexusmarket.adapters.persistence.jpa.repositories.UserJpaRepository;
import com.nexusmarket.domain.exceptions.ResourceNotFoundException;
import com.nexusmarket.domain.models.Buyer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
@RequiredArgsConstructor
public class BuyerJpaMapper {

    private final UserJpaRepository userJpaRepository;

    public Buyer toDomain(BuyerJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        return Buyer.builder()
                .id(entity.getId())
                .userId(entity.getUser() != null ? entity.getUser().getId() : null)
                .primaryAddress(entity.getPrimaryAddress())
                .additionalAddresses(entity.getAdditionalAddresses() != null ? new ArrayList<>(entity.getAdditionalAddresses()) : new ArrayList<>())
                .commercialStatus(entity.getCommercialStatus())
                .build();
    }

    public BuyerJpaEntity toEntity(Buyer domain) {
        if (domain == null) {
            return null;
        }
        UserJpaEntity user = null;
        if (domain.getUserId() != null) {
            user = userJpaRepository.findById(domain.getUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("User", domain.getUserId()));
        }

        return BuyerJpaEntity.builder()
                .id(domain.getId())
                .user(user)
                .primaryAddress(domain.getPrimaryAddress())
                .additionalAddresses(domain.getAdditionalAddresses() != null ? new ArrayList<>(domain.getAdditionalAddresses()) : new ArrayList<>())
                .commercialStatus(domain.getCommercialStatus())
                .build();
    }
}

