package com.nexusmarket.orders.domain.repository;

import com.nexusmarket.orders.domain.model.Order;
import com.nexusmarket.users.domain.model.Buyer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByBuyer(Buyer buyer);
}