package com.typingtutor.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String username;

    @Column(nullable = false, length = 80)
    private String action;

    @Column(length = 500)
    private String details;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public AuditLog() {}

    public AuditLog(String username, String action, String details) {
        this.username = username;
        this.action   = action;
        this.details  = details;
    }

    public Long getId()              { return id; }
    public String getUsername()      { return username; }
    public String getAction()        { return action; }
    public String getDetails()       { return details; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
