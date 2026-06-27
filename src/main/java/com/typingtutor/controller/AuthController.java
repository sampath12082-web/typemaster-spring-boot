package com.typingtutor.controller;

import com.typingtutor.dto.*;
import com.typingtutor.entity.User;
import com.typingtutor.security.PasswordCryptoService;
import com.typingtutor.security.PasswordPolicy;
import com.typingtutor.service.AuditLogService;
import com.typingtutor.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final PasswordCryptoService passwordCryptoService;
    private final AuditLogService auditLogService;

    public AuthController(UserService userService, PasswordCryptoService passwordCryptoService,
                           AuditLogService auditLogService) {
        this.userService = userService;
        this.passwordCryptoService = passwordCryptoService;
        this.auditLogService = auditLogService;
    }

    /** Public key the frontend encrypts passwords with before they ever leave the browser. */
    @GetMapping("/public-key")
    public ResponseEntity<Map<String, String>> publicKey() {
        return ResponseEntity.ok(Map.of("publicKey", passwordCryptoService.getPublicKeyBase64()));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest req) {
        String plainPassword = passwordCryptoService.decrypt(req.getPassword());
        PasswordPolicy.validate(plainPassword);
        req.setPassword(plainPassword);
        return ResponseEntity.ok(userService.register(req));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest req) {
        req.setPassword(passwordCryptoService.decrypt(req.getPassword()));
        return ResponseEntity.ok(userService.login(req));
    }

    @PostMapping("/verify-email")
    public ResponseEntity<AuthResponse> verifyEmail(@Valid @RequestBody OtpVerifyRequest req) {
        return ResponseEntity.ok(userService.verifyEmail(req.getEmail(), req.getOtp()));
    }

    @PostMapping("/resend-otp")
    public ResponseEntity<Map<String, Object>> resendOtp(@Valid @RequestBody ResendOtpRequest req) {
        return ResponseEntity.ok(userService.resendOtp(req.getEmail(), req.getPurpose()));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<Map<String, String>> verifyOtp(@Valid @RequestBody OtpVerifyRequest req) {
        String purpose = req.getPurpose() != null ? req.getPurpose() : "FIRST_LOGIN";
        return ResponseEntity.ok(userService.verifyOtp(req.getEmail(), req.getOtp(), purpose));
    }

    @PostMapping("/change-password")
    public ResponseEntity<Map<String, String>> changePassword(
            @Valid @RequestBody ChangePasswordRequest req) {
        String plainPassword = passwordCryptoService.decrypt(req.getNewPassword());
        PasswordPolicy.validate(plainPassword);
        userService.changePassword(req.getChangePasswordToken(), plainPassword);
        return ResponseEntity.ok(Map.of("message", "Password changed successfully."));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, Object>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest req) {
        return ResponseEntity.ok(userService.forgotPassword(req.getEmail()));
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.getUserByUsername(userDetails.getUsername());
        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("username",           user.getUsername());
        resp.put("userId",             user.getId());
        resp.put("role",               user.getRole().name());
        resp.put("email",              user.getEmail() != null ? user.getEmail() : "");
        resp.put("fullName",           user.getFullName() != null ? user.getFullName() : "");
        resp.put("dateOfBirth",        user.getDateOfBirth() != null ? user.getDateOfBirth().toString() : "");
        resp.put("student",            user.isStudent());
        resp.put("schoolName",         user.getSchoolName() != null ? user.getSchoolName() : "");
        resp.put("classYear",          user.getClassYear() != null ? user.getClassYear() : "");
        resp.put("courseSpecialization", user.getCourseSpecialization() != null ? user.getCourseSpecialization() : "");
        resp.put("occupation",         user.getOccupation() != null ? user.getOccupation() : "");
        resp.put("placementCompleted", userService.isEffectivePlacementCompleted(user));
        resp.put("recommendedTier",    user.getRecommendedTier() != null ? user.getRecommendedTier() : "");
        resp.put("emailVerified",               user.isEmailVerified());
        resp.put("emailVerificationDeadline",   user.getEmailVerificationDeadline() != null
                ? user.getEmailVerificationDeadline().toString() : null);
        UserStatsDto stats = userService.getUserStats(user);
        resp.put("averageWpm",        stats.getAverageWpm());
        resp.put("lessonsCompleted",  stats.getLessonsCompleted());
        resp.put("totalCompleted",    stats.getTotalCompleted());
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/ranking")
    public ResponseEntity<Map<String, Object>> ranking(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(userService.getRanking(userDetails.getUsername()));
    }

    @GetMapping("/my-activity")
    public ResponseEntity<List<Map<String, Object>>> myActivity(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
            auditLogService.getMyActivity(userDetails.getUsername()).stream()
                .map(log -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("action", log.getAction());
                    m.put("details", log.getDetails());
                    m.put("createdAt", log.getCreatedAt().toString());
                    return m;
                })
                .toList()
        );
    }

    @GetMapping("/leaderboard")
    public ResponseEntity<List<Map<String, Object>>> leaderboard() {
        return ResponseEntity.ok(userService.getLeaderboard());
    }

    @PutMapping("/me/password")
    public ResponseEntity<Map<String, String>> updatePassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UpdatePasswordRequest req) {
        String currentPassword = passwordCryptoService.decrypt(req.getCurrentPassword());
        String newPassword = passwordCryptoService.decrypt(req.getNewPassword());
        PasswordPolicy.validate(newPassword);
        userService.updatePassword(userDetails.getUsername(), currentPassword, newPassword);
        return ResponseEntity.ok(Map.of("message", "Password updated successfully."));
    }

    @PutMapping("/me")
    public ResponseEntity<?> updateProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ProfileUpdateRequest req) {
        ProfileUpdateResult result = userService.updateProfile(userDetails.getUsername(), req);
        User user = result.user();
        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("username",           user.getUsername());
        resp.put("email",              user.getEmail() != null ? user.getEmail() : "");
        resp.put("fullName",           user.getFullName() != null ? user.getFullName() : "");
        resp.put("dateOfBirth",        user.getDateOfBirth() != null ? user.getDateOfBirth().toString() : "");
        resp.put("student",            user.isStudent());
        resp.put("schoolName",         user.getSchoolName() != null ? user.getSchoolName() : "");
        resp.put("classYear",          user.getClassYear() != null ? user.getClassYear() : "");
        resp.put("courseSpecialization", user.getCourseSpecialization() != null ? user.getCourseSpecialization() : "");
        resp.put("occupation",         user.getOccupation() != null ? user.getOccupation() : "");
        resp.put("emailVerified",               user.isEmailVerified());
        resp.put("emailVerificationDeadline",   user.getEmailVerificationDeadline() != null
                ? user.getEmailVerificationDeadline().toString() : null);
        resp.put("emailChanged",       result.emailChanged());
        if (result.devOtp() != null) resp.put("devOtp", result.devOtp());
        if (result.emailWarning() != null) resp.put("emailWarning", result.emailWarning());
        resp.put("message",            "Profile updated successfully.");
        return ResponseEntity.ok(resp);
    }
}
