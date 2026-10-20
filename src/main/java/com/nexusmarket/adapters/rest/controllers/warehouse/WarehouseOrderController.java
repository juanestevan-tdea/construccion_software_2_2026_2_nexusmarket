package com.nexusmarket.adapters.rest.controllers.warehouse;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nexusmarket.adapters.rest.dtos.responses.OrderResponseDTO;
import com.nexusmarket.adapters.rest.mappers.OrderRestMapper;
import com.nexusmarket.domain.models.Order;
import com.nexusmarket.domain.ports.in.buyer.OrderUseCasePort;

import lombok.RequiredArgsConstructor;

/**
 * Transiciones logísticas de una orden.
 *
 * <p>Antes vivían bajo {@code /api/v1/buyer/orders}, por lo que un comprador
 * podía despachar, entregar y finalizar su propia orden. La ruta y el rol se
 * separaron para que solo un operador de bodega ejecute estas transiciones.</p>
 */
@RestController
@RequestMapping("/api/v1/warehouse/orders")
@PreAuthorize("hasRole('WAREHOUSE_OPERATOR')")
@RequiredArgsConstructor
public class WarehouseOrderController {

    private final OrderUseCasePort orderUseCasePort;

    @PatchMapping("/{id}/dispatch")
    public ResponseEntity<OrderResponseDTO> dispatch(@PathVariable Long id) {
        Order order = orderUseCasePort.dispatch(id);
        return ResponseEntity.ok(OrderRestMapper.toResponseDTO(order));
    }

    @PatchMapping("/{id}/deliver")
    public ResponseEntity<OrderResponseDTO> deliver(@PathVariable Long id) {
        Order order = orderUseCasePort.deliver(id);
        return ResponseEntity.ok(OrderRestMapper.toResponseDTO(order));
    }

    @PatchMapping("/{id}/finish")
    public ResponseEntity<OrderResponseDTO> finish(@PathVariable Long id) {
        Order order = orderUseCasePort.finish(id);
        return ResponseEntity.ok(OrderRestMapper.toResponseDTO(order));
    }
}
