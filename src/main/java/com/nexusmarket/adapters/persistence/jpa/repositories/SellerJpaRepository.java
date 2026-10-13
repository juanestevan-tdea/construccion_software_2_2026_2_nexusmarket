package com.nexusmarket.adapters.persistence.jpa.repositories;

import com.nexusmarket.adapters.persistence.jpa.entities.SellerJpaEntity;
import com.nexusmarket.adapters.persistence.jpa.entities.UserJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface SellerJpaRepository extends JpaRepository<SellerJpaEntity, Long> {

    Optional<SellerJpaEntity> findByTaxId(String taxId);

    List<SellerJpaEntity> findByActive(Boolean active);

    Optional<SellerJpaEntity> findByUser(UserJpaEntity user);

    @Query("SELECT s FROM SellerJpaEntity s WHERE s.user.id = :userId")
    Optional<SellerJpaEntity> findByUserId(@Param("userId") Long userId);
}
