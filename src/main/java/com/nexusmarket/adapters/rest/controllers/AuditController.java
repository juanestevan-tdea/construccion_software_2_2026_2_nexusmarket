package com.nexusmarket.adapters.rest.controllers;

import com.nexusmarket.adapters.rest.dtos.responses.AuditLogResponseDTO;
import com.nexusmarket.adapters.rest.mappers.AuditLogRestMapper;
import com.nexusmarket.domain.ports.in.AuditUseCasePort;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/audit")
@RequiredArgsConstructor
public class AuditController {

    private final AuditUseCasePort auditUseCasePort;

    @GetMapping
    public ResponseEntity<List<AuditLogResponseDTO>> getAllLogs(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String entityName,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {

        if (userId != null) {
            return ResponseEntity.ok(
                    auditUseCasePort.findByUser(userId).stream()
                            .map(AuditLogRestMapper::toResponseDTO)
                            .toList());
        }
        if (entityName != null && !entityName.isBlank()) {
            return ResponseEntity.ok(
                    auditUseCasePort.findByEntity(entityName).stream()
                            .map(AuditLogRestMapper::toResponseDTO)
                            .toList());
        }
        if (start != null && end != null) {
            return ResponseEntity.ok(
                    auditUseCasePort.findByDateRange(start, end).stream()
                            .map(AuditLogRestMapper::toResponseDTO)
                            .toList());
        }
        return ResponseEntity.ok(
                auditUseCasePort.findAll().stream()
                        .map(AuditLogRestMapper::toResponseDTO)
                        .toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AuditLogResponseDTO> getLogById(@PathVariable String id) {
        return ResponseEntity.ok(AuditLogRestMapper.toResponseDTO(auditUseCasePort.getByIdOrThrow(id)));
    }
}
