package com.nexusmarket.adapters.rest.controllers.supervisor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nexusmarket.adapters.rest.dtos.responses.ReturnResponseDTO;
import com.nexusmarket.adapters.rest.mappers.ReturnRestMapper;
import com.nexusmarket.domain.models.Return;
import com.nexusmarket.domain.ports.in.buyer.ReturnUseCasePort;

import lombok.RequiredArgsConstructor;

/**
 * Gestión de devoluciones por parte del supervisor.
 *
 * <p>Estas operaciones estaban publicadas bajo {@code /api/v1/buyer/returns},
 * lo que permitía a un comprador aprobar su propia devolución. Se movieron a
 * una ruta con rol SUPERVISOR.</p>
 */
@RestController
@RequestMapping("/api/v1/supervisor/returns")
@PreAuthorize("hasRole('SUPERVISOR')")
@RequiredArgsConstructor
public class ReturnManagementController {

    private final ReturnUseCasePort returnUseCasePort;

    @PatchMapping("/{id}/approve")
    public ResponseEntity<ReturnResponseDTO> approveReturn(@PathVariable Long id) {
        Return returnEntity = returnUseCasePort.approveReturn(id);
        return ResponseEntity.ok(ReturnRestMapper.toResponseDTO(returnEntity));
    }

    @PatchMapping("/{id}/reject")
    public ResponseEntity<ReturnResponseDTO> rejectReturn(@PathVariable Long id) {
        Return returnEntity = returnUseCasePort.rejectReturn(id);
        return ResponseEntity.ok(ReturnRestMapper.toResponseDTO(returnEntity));
    }

    @PatchMapping("/{id}/complete")
    public ResponseEntity<ReturnResponseDTO> completeReturn(@PathVariable Long id) {
        Return returnEntity = returnUseCasePort.completeReturn(id);
        return ResponseEntity.ok(ReturnRestMapper.toResponseDTO(returnEntity));
    }
}
