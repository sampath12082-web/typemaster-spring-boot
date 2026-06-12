package com.typingtutor.controller;

import com.typingtutor.dto.AdminCreateUserRequest;
import com.typingtutor.dto.AdminUserDto;
import com.typingtutor.dto.InquiryDto;
import com.typingtutor.dto.ResolveInquiryRequest;
import com.typingtutor.entity.AuditLog;
import com.typingtutor.repository.AuditLogRepository;
import com.typingtutor.service.AdminService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;
    private final AuditLogRepository auditLogRepository;

    public AdminController(AdminService adminService, AuditLogRepository auditLogRepository) {
        this.adminService = adminService;
        this.auditLogRepository = auditLogRepository;
    }

    @GetMapping("/users")
    public ResponseEntity<List<AdminUserDto>> getAllUsers() {
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    @PostMapping("/users")
    public ResponseEntity<AdminUserDto> createUser(@Valid @RequestBody AdminCreateUserRequest req) {
        return ResponseEntity.ok(adminService.createUser(req.getUsername(), req.getEmail(), req.getPassword()));
    }

    @DeleteMapping("/users/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
        adminService.deleteUser(userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/users/{userId}/toggle-active")
    public ResponseEntity<Map<String, Object>> toggleActive(@PathVariable Long userId) {
        boolean active = adminService.toggleActive(userId);
        return ResponseEntity.ok(Map.of("active", active));
    }

    @PostMapping("/users/{userId}/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(@PathVariable Long userId) {
        String tempPassword = adminService.resetPassword(userId);
        return ResponseEntity.ok(Map.of("temporaryPassword", tempPassword));
    }

    @GetMapping("/inquiries")
    public ResponseEntity<List<InquiryDto>> getAllInquiries() {
        return ResponseEntity.ok(adminService.getAllInquiries());
    }

    @PostMapping("/inquiries/{inquiryId}/resolve")
    public ResponseEntity<InquiryDto> resolveInquiry(
            @PathVariable Long inquiryId,
            @Valid @RequestBody ResolveInquiryRequest req) {
        return ResponseEntity.ok(adminService.resolveInquiry(inquiryId, req.getResponse()));
    }

    @GetMapping("/audit-logs")
    public ResponseEntity<List<AuditLog>> auditLogs() {
        return ResponseEntity.ok(auditLogRepository.findLatest(200));
    }
}
