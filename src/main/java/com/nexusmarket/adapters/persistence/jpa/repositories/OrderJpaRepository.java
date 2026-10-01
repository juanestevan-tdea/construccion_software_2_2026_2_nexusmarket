package com.nexusmarket.adapters.persistence.jpa.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.nexusmarket.adapters.persistence.jpa.entities.BuyerJpaEntity;
import com.nexusmarket.adapters.persistence.jpa.entities.OrderJpaEntity;

@Repository
public interface OrderJpaRepository extends JpaRepository<OrderJpaEntity, Long> {

    List<OrderJpaEntity> findByBuyer(BuyerJpaEntity buyer);

    @Query("SELECT o FROM OrderJpaEntity o WHERE o.buyer.id = :buyerId")
    List<OrderJpaEntity> findByBuyerId(@Param("buyerId") Long buyerId);
}
