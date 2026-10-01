package com.nexusmarket.adapters.persistence.jpa.repositories;

import com.nexusmarket.adapters.persistence.jpa.entities.BuyerJpaEntity;
import com.nexusmarket.adapters.persistence.jpa.entities.UserJpaEntity;
import com.nexusmarket.domain.valueobjects.BuyerCommercialStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BuyerJpaRepository extends JpaRepository<BuyerJpaEntity, Long> {

    List<BuyerJpaEntity> findByCommercialStatus(BuyerCommercialStatus status);

    Optional<BuyerJpaEntity> findByUser(UserJpaEntity user);

    @Query("SELECT b FROM BuyerJpaEntity b WHERE b.user.id = :userId")
    Optional<BuyerJpaEntity> findByUserId(@Param("userId") Long userId);
}
