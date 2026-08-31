package com.nexusmarket.audit.domain.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "audit_logs")
public class AuditLog {

    @Id
    private String id;

    private Long userId;
    private String action;
    private String timestamp;
    private String details;

    public AuditLog() {}

    // Getters y Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
}