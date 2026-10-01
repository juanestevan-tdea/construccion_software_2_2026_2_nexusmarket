package com.nexusmarket.adapters.persistence.jpa.repositories;

import com.nexusmarket.adapters.persistence.jpa.entities.OrderItemJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderItemJpaRepository extends JpaRepository<OrderItemJpaEntity, Long> {
}
