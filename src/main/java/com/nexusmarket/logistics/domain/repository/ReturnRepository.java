package com.nexusmarket.logistics.domain.repository;

import com.nexusmarket.logistics.domain.model.Return;
import com.nexusmarket.orders.domain.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReturnRepository extends JpaRepository<Return, Long> {
    List<Return> findByOrder(Order order);
    Optional<Return> findFirstByOrderOrderByRequestedAtDesc(Order order);
}