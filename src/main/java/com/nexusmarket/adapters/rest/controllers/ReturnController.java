package com.nexusmarket.adapters.rest.controllers;

import com.nexusmarket.adapters.rest.dtos.requests.ReturnCreateRequestDTO;
import com.nexusmarket.adapters.rest.dtos.responses.ReturnResponseDTO;
import com.nexusmarket.adapters.rest.mappers.ReturnRestMapper;
import com.nexusmarket.domain.models.Return;
import com.nexusmarket.domain.ports.in.ReturnUseCasePort;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/returns")
@RequiredArgsConstructor
public class ReturnController {

    private final ReturnUseCasePort returnUseCasePort;

    @PostMapping
    public ResponseEntity<ReturnResponseDTO> createReturn(@Valid @RequestBody ReturnCreateRequestDTO request) {
        Return returnEntity = returnUseCasePort.createReturn(request.getOrderId(), request.getReason());
        return new ResponseEntity<>(ReturnRestMapper.toResponseDTO(returnEntity), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReturnResponseDTO> getReturnById(@PathVariable Long id) {
        Return returnEntity = returnUseCasePort.getByIdOrThrow(id);
        return ResponseEntity.ok(ReturnRestMapper.toResponseDTO(returnEntity));
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<ReturnResponseDTO>> getReturnsByOrder(@PathVariable Long orderId) {
        List<ReturnResponseDTO> list = returnUseCasePort.findByOrder(orderId).stream()
                .map(ReturnRestMapper::toResponseDTO)
                .toList();
        return ResponseEntity.ok(list);
    }

    @GetMapping
    public ResponseEntity<List<ReturnResponseDTO>> getAllReturns() {
        List<ReturnResponseDTO> list = returnUseCasePort.findAll().stream()
                .map(ReturnRestMapper::toResponseDTO)
                .toList();
        return ResponseEntity.ok(list);
    }

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
