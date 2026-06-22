package com.typingtutor.service;

import com.typingtutor.dto.AdminUserDto;
import com.typingtutor.dto.InquiryDto;
import com.typingtutor.entity.*;
import com.typingtutor.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
public class AdminService {

    private static final Logger log = LoggerFactory.getLogger(AdminService.class);
    private static final String UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWER = "abcdefghijklmnopqrstuvwxyz";
    private static final String DIGITS = "0123456789";
    private static final String SPECIAL = "@$!%*?&";
    private static final String ALL_CHARS = UPPER + LOWER + DIGITS + SPECIAL;

    @Value("${app.dev-otp-enabled:false}")
    private boolean devOtpEnabled;

    private final UserRepository userRepository;
    private final UserPerformanceRepository performanceRepository;
    private final InquiryRepository inquiryRepository;
    private final ExamAttemptRepository examAttemptRepository;
    private final CertificateRepository certificateRepository;
    private final EmailVerificationRepository emailVerificationRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;
    private final OtpService otpService;
    private final EmailService emailService;

    public AdminService(UserRepository userRepository,
                        UserPerformanceRepository performanceRepository,
                        InquiryRepository inquiryRepository,
                        ExamAttemptRepository examAttemptRepository,
                        CertificateRepository certificateRepository,
                        EmailVerificationRepository emailVerificationRepository,
                        PasswordEncoder passwordEncoder,
                        AuditLogService auditLogService,
                        OtpService otpService,
                        EmailService emailService) {
        this.userRepository = userRepository;
        this.performanceRepository = performanceRepository;
        this.inquiryRepository = inquiryRepository;
        this.examAttemptRepository = examAttemptRepository;
        this.certificateRepository = certificateRepository;
        this.emailVerificationRepository = emailVerificationRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditLogService = auditLogService;
        this.otpService = otpService;
        this.emailService = emailService;
    }

    public List<AdminUserDto> getAllUsers() {
        Map<Long, List<UserPerformance>> perfsByUser = performanceRepository.findAllWithUser()
            .stream().collect(Collectors.groupingBy(p -> p.getUser().getId()));

        return userRepository.findAll().stream()
            .filter(u -> u.getRole() != Role.ADMIN)
            .map(u -> {
                List<UserPerformance> perfs = perfsByUser.getOrDefault(u.getId(), Collections.emptyList());
                AdminUserDto dto = new AdminUserDto();
                dto.setId(u.getId());
                dto.setUsername(u.getUsername());
                dto.setEmail(u.getEmail());
                dto.setRole(u.getRole().name());
                dto.setActive(u.isActive());
                dto.setTotalRuns(perfs.size());
                if (!perfs.isEmpty()) {
                    dto.setAvgWpm(perfs.stream().mapToInt(UserPerformance::getWpm).average().orElse(0));
                    dto.setAvgAccuracy(perfs.stream().mapToDouble(UserPerformance::getAccuracyPercentage).average().orElse(0));
                    dto.setBestWpm(perfs.stream().mapToInt(UserPerformance::getWpm).max().orElse(0));
                }
                return dto;
            }).collect(Collectors.toList());
    }

    @Transactional
    public AdminUserDto createUser(String username, String email, String password, String adminUsername) {
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username already taken: " + username);
        }
        if (email != null && !email.isBlank() && userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already registered: " + email);
        }
        // Admin-created users must change password on first login
        User user = User.builder()
            .username(username)
            .email(email)
            .password(passwordEncoder.encode(password))
            .role(Role.USER)
            .emailVerified(true)        // admin-created users skip email verification
            .passwordChanged(false)     // force password change on first login
            .placementCompleted(true)   // admin-created users skip placement test
            .build();
        user = userRepository.save(user);
        auditLogService.log(adminUsername, "USER_CREATE", "Created user: " + username);
        AdminUserDto dto = new AdminUserDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole().name());
        return dto;
    }

    @Transactional
    public boolean toggleActive(Long userId, String adminUsername) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new NoSuchElementException("User not found: " + userId));
        user.setActive(!user.isActive());
        userRepository.save(user);
        auditLogService.log(adminUsername, user.isActive() ? "USER_ACTIVATE" : "USER_DEACTIVATE", "User: " + user.getUsername());
        return user.isActive();
    }

    @Transactional
    public void deleteUser(Long userId, String adminUsername) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new NoSuchElementException("User not found: " + userId));
        String username = user.getUsername();
        // Delete in FK dependency order: deepest child first
        certificateRepository.deleteByUserId(userId);
        examAttemptRepository.deleteByUserId(userId);
        performanceRepository.deleteByUserId(userId);
        inquiryRepository.deleteByUserId(userId);
        emailVerificationRepository.deleteByUserId(userId);
        userRepository.deleteById(userId);
        auditLogService.log(adminUsername, "USER_DELETE", "Deleted user: " + username);
    }

    @Transactional
    public Map<String, Object> resetPassword(Long userId, String adminUsername) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new NoSuchElementException("User not found: " + userId));
        Map<String, Object> result = new HashMap<>();
        boolean hasEmail = user.getEmail() != null && !user.getEmail().isBlank();

        if (hasEmail) {
            // Send OTP to user's email so they can set their own new password
            EmailVerification ev = otpService.createOtp(user.getId(), "RESET_PASSWORD");
            String displayName = (user.getFullName() != null && !user.getFullName().isBlank())
                    ? user.getFullName() : user.getUsername();
            boolean emailSent = emailService.sendOtp(user.getEmail(), ev.getOtpCode(), "RESET_PASSWORD", displayName);
            if (devOtpEnabled) {
                result.put("devOtp", ev.getOtpCode());
            } else if (!emailSent) {
                result.put("emailWarning", "OTP email could not be sent. Check MAIL_PASSWORD configuration.");
            }
            result.put("otpSent", true);
            result.put("message", "Password reset OTP sent to user's email address.");
            auditLogService.log(adminUsername, "PASSWORD_RESET_OTP", "Sent reset OTP to: " + user.getEmail());
        } else {
            // No email — generate a temporary password the admin can share directly
            String tempPassword = generateTemporaryPassword();
            user.setPassword(passwordEncoder.encode(tempPassword));
            user.setPasswordChanged(true);  // no forced change; admin provides temp directly
            userRepository.save(user);
            result.put("temporaryPassword", tempPassword);
            result.put("message", "Temporary password set. Share it through a secure channel.");
            auditLogService.log(adminUsername, "PASSWORD_RESET", "Temp password set for: " + user.getUsername());
        }
        return result;
    }

    public List<InquiryDto> getAllInquiries() {
        return inquiryRepository.findAllByOrderByCreatedAtDesc().stream()
            .map(InquiryDto::from)
            .collect(Collectors.toList());
    }

    public InquiryDto resolveInquiry(Long inquiryId, String response, String adminUsername) {
        Inquiry inq = inquiryRepository.findById(inquiryId)
            .orElseThrow(() -> new NoSuchElementException("Inquiry not found: " + inquiryId));
        inq.setStatus(InquiryStatus.RESOLVED);
        inq.setAdminResponse(response);
        InquiryDto result = InquiryDto.from(inquiryRepository.save(inq));
        auditLogService.log(adminUsername, "INQUIRY_RESOLVED", "inquiryId=" + inquiryId);
        return result;
    }

    private String generateTemporaryPassword() {
        SecureRandom random = new SecureRandom();
        // Guarantee all required categories appear, then pad to 16 chars (matches password policy)
        List<Character> chars = new java.util.ArrayList<>();
        chars.add(UPPER.charAt(random.nextInt(UPPER.length())));
        chars.add(LOWER.charAt(random.nextInt(LOWER.length())));
        chars.add(DIGITS.charAt(random.nextInt(DIGITS.length())));
        chars.add(SPECIAL.charAt(random.nextInt(SPECIAL.length())));
        for (int i = 4; i < 16; i++) {
            chars.add(ALL_CHARS.charAt(random.nextInt(ALL_CHARS.length())));
        }
        Collections.shuffle(chars, random);
        StringBuilder sb = new StringBuilder(16);
        for (char c : chars) sb.append(c);
        return sb.toString();
    }
}
