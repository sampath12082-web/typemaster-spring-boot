package com.typingtutor.service;

import com.typingtutor.dto.AdminUserDto;
import com.typingtutor.dto.InquiryDto;
import com.typingtutor.entity.*;
import com.typingtutor.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Collections;
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
    private static final String SPECIAL = "!@#$";
    private static final String ALL_CHARS = UPPER + LOWER + DIGITS + SPECIAL;

    private final UserRepository userRepository;
    private final UserPerformanceRepository performanceRepository;
    private final InquiryRepository inquiryRepository;
    private final ExamAttemptRepository examAttemptRepository;
    private final CertificateRepository certificateRepository;
    private final EmailVerificationRepository emailVerificationRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;

    public AdminService(UserRepository userRepository,
                        UserPerformanceRepository performanceRepository,
                        InquiryRepository inquiryRepository,
                        ExamAttemptRepository examAttemptRepository,
                        CertificateRepository certificateRepository,
                        EmailVerificationRepository emailVerificationRepository,
                        PasswordEncoder passwordEncoder,
                        AuditLogService auditLogService) {
        this.userRepository = userRepository;
        this.performanceRepository = performanceRepository;
        this.inquiryRepository = inquiryRepository;
        this.examAttemptRepository = examAttemptRepository;
        this.certificateRepository = certificateRepository;
        this.emailVerificationRepository = emailVerificationRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditLogService = auditLogService;
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

    public void resetPassword(Long userId, String adminUsername) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new NoSuchElementException("User not found: " + userId));
        String tempPassword = generateTemporaryPassword();
        user.setPassword(passwordEncoder.encode(tempPassword));
        // No-email users can't complete the OTP-based forced-change flow, so treat
        // the admin-reset as their final password.
        boolean hasEmail = user.getEmail() != null && !user.getEmail().isBlank();
        user.setPasswordChanged(!hasEmail);
        userRepository.save(user);
        auditLogService.log(adminUsername, "PASSWORD_RESET", "Reset password for: " + user.getUsername());
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
        // Guarantee all required categories appear, then pad to 12 chars
        List<Character> chars = new java.util.ArrayList<>();
        chars.add(UPPER.charAt(random.nextInt(UPPER.length())));
        chars.add(LOWER.charAt(random.nextInt(LOWER.length())));
        chars.add(DIGITS.charAt(random.nextInt(DIGITS.length())));
        chars.add(SPECIAL.charAt(random.nextInt(SPECIAL.length())));
        for (int i = 4; i < 12; i++) {
            chars.add(ALL_CHARS.charAt(random.nextInt(ALL_CHARS.length())));
        }
        Collections.shuffle(chars, random);
        StringBuilder sb = new StringBuilder(12);
        for (char c : chars) sb.append(c);
        return sb.toString();
    }
}
