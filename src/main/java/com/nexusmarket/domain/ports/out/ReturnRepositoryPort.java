package com.nexusmarket.domain.ports.out;

import com.nexusmarket.domain.models.Return;

import java.util.List;
import java.util.Optional;

public interface ReturnRepositoryPort {

    Return save(Return returnEntity);

    Optional<Return> findById(Long id);

    boolean existsById(Long id);

    List<Return> findByOrderId(Long orderId);

    Optional<Return> findFirstByOrderIdOrderByRequestedAtDesc(Long orderId);

    List<Return> findAll();
}
