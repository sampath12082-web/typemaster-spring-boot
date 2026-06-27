package com.typingtutor.controller;

import com.typingtutor.dto.AdminCreateUserRequest;
import com.typingtutor.dto.AdminUserDto;
import com.typingtutor.dto.InquiryDto;
import com.typingtutor.dto.ResolveInquiryRequest;
import com.typingtutor.entity.AuditLog;
import com.typingtutor.security.PasswordCryptoService;
import com.typingtutor.security.PasswordPolicy;
import com.typingtutor.service.AdminService;
import com.typingtutor.service.AuditLogService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;
    private final AuditLogService auditLogService;
    private final PasswordCryptoService passwordCryptoService;

    public AdminController(AdminService adminService, AuditLogService auditLogService,
                           PasswordCryptoService passwordCryptoService) {
        this.adminService = adminService;
        this.auditLogService = auditLogService;
        this.passwordCryptoService = passwordCryptoService;
    }

    @GetMapping("/users")
    public ResponseEntity<List<AdminUserDto>> getAllUsers() {
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    @PostMapping("/users")
    public ResponseEntity<AdminUserDto> createUser(@AuthenticationPrincipal UserDetails principal,
                                                   @Valid @RequestBody AdminCreateUserRequest req) {
        String plainPassword = passwordCryptoService.decrypt(req.getPassword());
        PasswordPolicy.validate(plainPassword);
        return ResponseEntity.ok(adminService.createUser(req.getUsername(), req.getEmail(), plainPassword,
                principal.getUsername()));
    }

    @DeleteMapping("/users/{userId}")
    public ResponseEntity<Void> deleteUser(@AuthenticationPrincipal UserDetails principal,
                                           @PathVariable Long userId) {
        adminService.deleteUser(userId, principal.getUsername());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/users/{userId}/toggle-active")
    public ResponseEntity<Map<String, Object>> toggleActive(@AuthenticationPrincipal UserDetails principal,
                                                            @PathVariable Long userId) {
        boolean active = adminService.toggleActive(userId, principal.getUsername());
        return ResponseEntity.ok(Map.of("active", active));
    }

    @PostMapping("/users/{userId}/reset-password")
    public ResponseEntity<Map<String, Object>> resetPassword(@AuthenticationPrincipal UserDetails principal,
                                                             @PathVariable Long userId) {
        return ResponseEntity.ok(adminService.resetPassword(userId, principal.getUsername()));
    }

    @GetMapping("/inquiries")
    public ResponseEntity<List<InquiryDto>> getAllInquiries() {
        return ResponseEntity.ok(adminService.getAllInquiries());
    }

    @PostMapping("/inquiries/{inquiryId}/resolve")
    public ResponseEntity<InquiryDto> resolveInquiry(@AuthenticationPrincipal UserDetails principal,
            @PathVariable Long inquiryId,
            @Valid @RequestBody ResolveInquiryRequest req) {
        return ResponseEntity.ok(adminService.resolveInquiry(inquiryId, req.getResponse(), principal.getUsername()));
    }

    @GetMapping("/audit-logs")
    public ResponseEntity<List<AuditLog>> auditLogs() {
        return ResponseEntity.ok(auditLogService.getLatest(200));
    }
}
