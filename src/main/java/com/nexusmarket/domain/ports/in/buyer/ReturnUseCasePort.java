package com.nexusmarket.domain.ports.in.buyer;

import com.nexusmarket.domain.models.Return;

import java.util.List;

public interface ReturnUseCasePort {

    Return createReturn(Long orderId, String reason);

    Return getByIdOrThrow(Long id);

    List<Return> findByOrder(Long orderId);

    List<Return> findAllByBuyerId(Long buyerId);

    List<Return> findAll();

    Return approveReturn(Long id);

    Return rejectReturn(Long id);

    Return completeReturn(Long id);
}
