package com.nexusmarket.logistics.domain.repository;

import com.nexusmarket.logistics.domain.model.Return;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReturnRepository extends JpaRepository<Return, Long> {
}