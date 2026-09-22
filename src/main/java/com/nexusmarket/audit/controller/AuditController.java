package com.nexusmarket.audit.controller;

import com.nexusmarket.audit.dto.AuditLogResponse;
import com.nexusmarket.audit.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/audit")
@RequiredArgsConstructor
public class AuditController {

    private final AuditService auditService;

    @GetMapping
    public ResponseEntity<List<AuditLogResponse>> getAllLogs(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String entityName,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {

        if (userId != null) {
            return ResponseEntity.ok(auditService.findByUser(userId));
        }
        if (entityName != null && !entityName.isBlank()) {
            return ResponseEntity.ok(auditService.findByEntity(entityName));
        }
        if (start != null && end != null) {
            return ResponseEntity.ok(auditService.findByDateRange(start, end));
        }
        return ResponseEntity.ok(auditService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AuditLogResponse> getLogById(@PathVariable String id) {
        return ResponseEntity.ok(auditService.getAuditLogResponseById(id));
    }
}
