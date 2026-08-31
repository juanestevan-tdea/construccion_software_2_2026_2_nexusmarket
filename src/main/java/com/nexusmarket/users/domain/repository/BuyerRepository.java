package com.nexusmarket.users.domain.repository;

import com.nexusmarket.users.domain.model.Buyer;
import com.nexusmarket.users.domain.model.BuyerCommercialStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BuyerRepository extends JpaRepository<Buyer, Long> {
    List<Buyer> findByCommercialStatus(BuyerCommercialStatus status);
}