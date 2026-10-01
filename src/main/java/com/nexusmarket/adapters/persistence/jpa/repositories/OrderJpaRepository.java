package com.nexusmarket.adapters.persistence.jpa.repositories;

import com.nexusmarket.adapters.persistence.jpa.entities.OrderJpaEntity;
import com.nexusmarket.users.domain.model.Buyer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderJpaRepository extends JpaRepository<OrderJpaEntity, Long> {

    List<OrderJpaEntity> findByBuyer(Buyer buyer);

    @Query("SELECT o FROM OrderJpaEntity o WHERE o.buyer.id = :buyerId")
    List<OrderJpaEntity> findByBuyerId(@Param("buyerId") Long buyerId);
}
