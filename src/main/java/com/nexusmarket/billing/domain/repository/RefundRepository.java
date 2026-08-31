package com.nexusmarket.billing.domain.repository;

import com.nexusmarket.billing.domain.model.Refund;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RefundRepository extends JpaRepository<Refund, Long> {
}
