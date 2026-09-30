package com.nexusmarket.adapters.useCases;

import com.nexusmarket.common.exception.ResourceNotFoundException;
import com.nexusmarket.domain.models.Return;
import com.nexusmarket.domain.ports.in.ReturnUseCasePort;
import com.nexusmarket.domain.ports.out.ReturnRepositoryPort;
import com.nexusmarket.domain.services.ReturnProcessService;
import com.nexusmarket.orders.domain.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReturnUseCaseImpl implements ReturnUseCasePort {

    private final ReturnProcessService returnProcessService;
    private final ReturnRepositoryPort returnRepositoryPort;
    private final OrderRepository orderRepository;

    @Override
    public Return createReturn(Long orderId, String reason) {
        return returnProcessService.createReturn(orderId, reason);
    }

    @Override
    public Return getByIdOrThrow(Long id) {
        return returnRepositoryPort.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Return", id));
    }

    @Override
    public List<Return> findByOrder(Long orderId) {
        if (!orderRepository.existsById(orderId)) {
            throw new ResourceNotFoundException("Order", orderId);
        }
        return returnRepositoryPort.findByOrderId(orderId);
    }

    @Override
    public List<Return> findAll() {
        return returnRepositoryPort.findAll();
    }

    @Override
    public Return approveReturn(Long id) {
        return returnProcessService.approveReturn(id);
    }

    @Override
    public Return rejectReturn(Long id) {
        return returnProcessService.rejectReturn(id);
    }

    @Override
    public Return completeReturn(Long id) {
        return returnProcessService.completeReturn(id);
    }
}
