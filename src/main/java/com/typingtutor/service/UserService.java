package com.typingtutor.service;

import com.typingtutor.dto.*;
import com.typingtutor.entity.User;
import com.typingtutor.entity.EmailVerification;
import com.typingtutor.entity.UserPerformance;
import com.typingtutor.repository.UserPerformanceRepository;
import com.typingtutor.repository.UserRepository;
import com.typingtutor.security.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final UserPerformanceRepository performanceRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final OtpService otpService;
    private final EmailService emailService;
    private final AuditLogService auditLogService;

    // Deliberately decoupled from EmailService.isMailEnabled(): devOtp must only ever be exposed
    // in a local/dev properties file, never as a fallback for misconfigured mail in any other
    // environment (that would leak OTP codes directly in API responses).
    @Value("${app.dev-otp-enabled:false}")
    private boolean devOtpEnabled;

    private static final String EMAIL_SEND_FAILED_WARNING =
            "We couldn't send the email right now. Please try \"Resend OTP\" in a few minutes.";

    public UserService(UserRepository userRepository,
                       UserPerformanceRepository performanceRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil,
                       AuthenticationManager authenticationManager,
                       OtpService otpService,
                       EmailService emailService,
                       AuditLogService auditLogService) {
        this.userRepository = userRepository;
        this.performanceRepository = performanceRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
        this.otpService = otpService;
        this.emailService = emailService;
        this.auditLogService = auditLogService;
    }

    @Transactional
    public Map<String, Object> register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already taken: " + request.getUsername());
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already registered: " + request.getEmail());
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .dateOfBirth(request.getDateOfBirth())
                .student(request.isStudent())
                .schoolName(request.getSchoolName())
                .classYear(request.getClassYear())
                .courseSpecialization(request.getCourseSpecialization())
                .occupation(request.getOccupation())
                .emailVerified(false)
                .passwordChanged(true)
                .build();
        user = userRepository.save(user);

        EmailVerification ev = otpService.createOtp(user.getId(), "VERIFY_EMAIL");
        boolean emailSent = emailService.sendOtp(user.getEmail(), ev.getOtpCode(), "VERIFY_EMAIL", user.getUsername());
        log.debug("Registration OTP send attempt for user={} — sent={}", user.getUsername(), emailSent);

        Map<String, Object> result = new HashMap<>();
        result.put("message", "Registration successful. Please verify your email with the OTP sent to " + user.getEmail());
        result.put("email", user.getEmail());
        if (devOtpEnabled) {
            result.put("devOtp", ev.getOtpCode());
        } else if (!emailSent) {
            result.put("emailWarning", EMAIL_SEND_FAILED_WARNING);
        }
        return result;
    }

    @Transactional
    public AuthResponse verifyEmail(String email, String otp) {
        Optional<EmailVerification> evOpt = otpService.findValidOtp(email, otp, "VERIFY_EMAIL");
        if (evOpt.isEmpty()) {
            throw new IllegalArgumentException("Invalid or expired OTP.");
        }
        EmailVerification ev = evOpt.get();
        User user = ev.getUser();
        user.setEmailVerified(true);
        user.setEmailVerificationDeadline(null);
        userRepository.save(user);
        otpService.markUsed(ev.getId());

        emailService.sendWelcome(user.getEmail(), user.getUsername());
        auditLogService.log(user.getUsername(), "EMAIL_VERIFIED", "Email verified: " + user.getEmail());
        String token = jwtUtil.generateToken(user.getUsername(), user.getRole().name());
        log.debug("Email verified for user={}", user.getUsername());
        return new AuthResponse(token, user.getUsername(), user.getId(), user.getRole().name(),
                true, isEffectivePlacementCompleted(user));
    }

    @Transactional
    public Object login(LoginRequest request) {
        log.info("[LOGIN] Attempt for username='{}'", request.getUsername());

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );
        } catch (Exception ex) {
            log.warn("[LOGIN] Authentication failed for username='{}' — {}: {}",
                    request.getUsername(), ex.getClass().getSimpleName(), ex.getMessage());
            throw ex;
        }

        log.info("[LOGIN] Password OK for username='{}'", request.getUsername());

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        log.info("[LOGIN] User state — id={}, active={}, emailVerified={}, passwordChanged={}, email='{}'",
                user.getId(), user.isActive(), user.isEmailVerified(),
                user.isPasswordChanged(), user.getEmail());

        if (!user.isActive()) {
            log.warn("[LOGIN] Blocked — account inactive for username='{}'", request.getUsername());
            throw new IllegalArgumentException("ACCOUNT_INACTIVE");
        }

        // Only block for unverified email when the user actually has one — no-email users
        // are allowed in and reminded via the dashboard popup instead.
        String userEmail = user.getEmail();
        boolean hasEmail = userEmail != null && !userEmail.isBlank();
        log.info("[LOGIN] hasEmail={} for username='{}'", hasEmail, request.getUsername());

        if (hasEmail && !user.isEmailVerified() && user.getRole() != com.typingtutor.entity.Role.ADMIN) {
            if (user.getEmailVerificationDeadline() != null &&
                    user.getEmailVerificationDeadline().isBefore(LocalDateTime.now())) {
                user.setActive(false);
                userRepository.save(user);
                auditLogService.log(user.getUsername(), "EMAIL_VERIFICATION_EXPIRED",
                        "Account disabled — email not verified within grace period");
                throw new IllegalArgumentException("ACCOUNT_INACTIVE");
            }
            log.warn("[LOGIN] Blocked — email not verified for username='{}'", request.getUsername());
            throw new EmailNotVerifiedException("EMAIL_NOT_VERIFIED", user.getEmail());
        }

        // First-login: force password change via OTP — skip for no-email users (can't send OTP)
        if (!user.isPasswordChanged() && hasEmail) {
            log.info("[LOGIN] First-login OTP flow triggered for username='{}'", request.getUsername());
            EmailVerification ev = otpService.createOtp(user.getId(), "FIRST_LOGIN");
            boolean emailSent = emailService.sendOtp(user.getEmail(), ev.getOtpCode(), "FIRST_LOGIN", user.getUsername());
            Map<String, Object> firstLogin = new HashMap<>();
            firstLogin.put("requiresPasswordChange", true);
            firstLogin.put("email", user.getEmail());
            firstLogin.put("message", "A one-time password has been sent to your email. Please verify to continue.");
            if (devOtpEnabled) {
                firstLogin.put("devOtp", ev.getOtpCode());
            } else if (!emailSent) {
                firstLogin.put("emailWarning", EMAIL_SEND_FAILED_WARNING);
            }
            return firstLogin;
        }

        log.info("[LOGIN] Success — issuing token for username='{}'", request.getUsername());
        auditLogService.log(user.getUsername(), "LOGIN", "Successful login");
        String token = jwtUtil.generateToken(user.getUsername(), user.getRole().name());
        return new AuthResponse(token, user.getUsername(), user.getId(), user.getRole().name(),
                user.isEmailVerified(), isEffectivePlacementCompleted(user));
    }

    @Transactional
    public Map<String, String> verifyOtp(String email, String otp, String purpose) {
        Optional<EmailVerification> evOpt = otpService.findValidOtp(email, otp, purpose);
        if (evOpt.isEmpty()) {
            throw new IllegalArgumentException("Invalid or expired OTP.");
        }
        EmailVerification ev = evOpt.get();
        otpService.markUsed(ev.getId());
        String changePasswordToken = jwtUtil.generateChangePasswordToken(ev.getUser().getUsername());
        return Map.of("changePasswordToken", changePasswordToken);
    }

    @Transactional
    public void changePassword(String changePasswordToken, String newPassword) {
        String username = jwtUtil.extractChangePasswordUsername(changePasswordToken);
        if (username == null) {
            throw new IllegalArgumentException("Invalid or expired token.");
        }
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NoSuchElementException("User not found"));
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordChanged(true);
        userRepository.save(user);
        log.debug("Password changed for user={}", username);
        auditLogService.log(username, "PASSWORD_CHANGED", "via OTP flow");
    }

    @Transactional
    public void updatePassword(String username, String currentPassword, String newPassword) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NoSuchElementException("User not found"));
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new IllegalArgumentException("Current password is incorrect.");
        }
        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new IllegalArgumentException("New password must be different from your current password.");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        auditLogService.log(username, "PASSWORD_UPDATED", "Password changed by authenticated user");
    }

    @Transactional
    public Map<String, Object> forgotPassword(String email) {
        Map<String, Object> result = new HashMap<>();
        result.put("message", "If an account exists for that email, a reset OTP has been sent.");
        userRepository.findByEmail(email).ifPresent(user -> {
            EmailVerification ev = otpService.createOtp(user.getId(), "RESET_PASSWORD");
            boolean emailSent = emailService.sendOtp(user.getEmail(), ev.getOtpCode(), "RESET_PASSWORD", user.getUsername());
            if (devOtpEnabled) {
                result.put("devOtp", ev.getOtpCode());
            } else if (!emailSent) {
                result.put("emailWarning", EMAIL_SEND_FAILED_WARNING);
            }
        });
        return result;
    }

    @Transactional
    public Map<String, Object> resendOtp(String email, String purpose) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("No account found for that email."));
        EmailVerification ev = otpService.createOtp(user.getId(), purpose);
        boolean emailSent = emailService.sendOtp(user.getEmail(), ev.getOtpCode(), purpose, user.getUsername());

        Map<String, Object> result = new HashMap<>();
        result.put("message", "OTP resent to " + email);
        if (devOtpEnabled) {
            result.put("devOtp", ev.getOtpCode());
        } else if (!emailSent) {
            result.put("emailWarning", EMAIL_SEND_FAILED_WARNING);
        }
        return result;
    }

    @Transactional
    public ProfileUpdateResult updateProfile(String username, ProfileUpdateRequest req) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NoSuchElementException("User not found: " + username));
        boolean emailChanged = false;
        String devOtp = null;
        String emailWarning = null;
        if (req.getFullName() != null) user.setFullName(req.getFullName());
        if (req.getEmail() != null && !req.getEmail().isBlank()
                && !req.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(req.getEmail())) {
                throw new IllegalArgumentException("Email already registered: " + req.getEmail());
            }
            user.setEmail(req.getEmail());
            user.setEmailVerified(false);
            user.setEmailVerificationDeadline(LocalDateTime.now().plusDays(2));
            EmailVerification ev = otpService.createOtp(user.getId(), "VERIFY_EMAIL");
            boolean emailSent = emailService.sendOtp(req.getEmail(), ev.getOtpCode(), "VERIFY_EMAIL", user.getUsername());
            if (devOtpEnabled) {
                devOtp = ev.getOtpCode();
            } else if (!emailSent) {
                emailWarning = EMAIL_SEND_FAILED_WARNING;
            }
            auditLogService.log(username, "EMAIL_UPDATED", "Email changed to: " + req.getEmail());
            emailChanged = true;
        }
        if (req.getDateOfBirth() != null) user.setDateOfBirth(req.getDateOfBirth());
        if (req.getStudent() != null) user.setStudent(req.getStudent());
        if (req.getSchoolName() != null) user.setSchoolName(req.getSchoolName());
        if (req.getClassYear() != null) user.setClassYear(req.getClassYear());
        if (req.getCourseSpecialization() != null) user.setCourseSpecialization(req.getCourseSpecialization());
        if (req.getOccupation() != null) user.setOccupation(req.getOccupation());
        return new ProfileUpdateResult(userRepository.save(user), devOtp, emailChanged, emailWarning);
    }

    @Transactional(readOnly = true)
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new NoSuchElementException("User not found: " + username));
    }

    // Legacy users who were created before the placement system have placementCompleted=false
    // even though they've already done lessons. Treat them as completed if they have any records.
    @Transactional(readOnly = true)
    public boolean isEffectivePlacementCompleted(User user) {
        return user.isPlacementCompleted() || performanceRepository.existsByUserId(user.getId());
    }

    @Transactional(readOnly = true)
    public UserStatsDto getUserStats(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NoSuchElementException("User not found: " + username));
        return getUserStats(user);
    }

    @Transactional(readOnly = true)
    public UserStatsDto getUserStats(User user) {
        UserStatsDto stats = new UserStatsDto();
        stats.setUsername(user.getUsername());
        stats.setLessonsCompleted((int) performanceRepository.countDistinctLessonsByUserId(user.getId()));
        Double avgWpm = performanceRepository.findAverageWpmByUserId(user.getId());
        stats.setAverageWpm(avgWpm != null ? Math.round(avgWpm * 10.0) / 10.0 : 0.0);
        stats.setTotalCompleted((int) performanceRepository.countByUserId(user.getId()));
        return stats;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getRanking(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NoSuchElementException("User not found"));
        long higher    = performanceRepository.countUsersRankedHigher(user.getId());
        long typists   = performanceRepository.countTypists();
        long totalUsers = userRepository.count();
        int  rank       = (int) higher + 1;
        int  bestWpm    = 0;
        List<UserPerformance> bests = performanceRepository.findByUserIdOrderByWpmDesc(user.getId());
        if (!bests.isEmpty()) bestWpm = bests.get(0).getWpm();
        int percentile = typists > 0 ? (int) Math.round(100.0 * (typists - higher) / typists) : 100;
        Map<String, Object> r = new java.util.LinkedHashMap<>();
        r.put("rank",        rank);
        r.put("totalTypists", (int) typists);
        r.put("totalUsers",  (int) totalUsers);
        r.put("userBestWpm", bestWpm);
        r.put("percentile",  percentile);
        return r;
    }

    @Cacheable("leaderboard")
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getLeaderboard() {
        return performanceRepository.findLeaderboard().stream()
                .map(row -> {
                    Map<String, Object> entry = new java.util.LinkedHashMap<>();
                    entry.put("userId", row[0]);
                    String fullName = row[2] != null ? row[2].toString() : "";
                    entry.put("displayName", fullName.isBlank() ? row[1].toString() : fullName);
                    entry.put("bestWpm", ((Number) row[3]).intValue());
                    entry.put("avgAccuracy", ((Number) row[4]).doubleValue());
                    entry.put("totalRuns", ((Number) row[5]).longValue());
                    return entry;
                })
                .toList();
    }

    public static class EmailNotVerifiedException extends RuntimeException {
        private final String email;

        public EmailNotVerifiedException(String message) {
            this(message, null);
        }

        public EmailNotVerifiedException(String message, String email) {
            super(message);
            this.email = email;
        }

        public String getEmail() {
            return email;
        }
    }
}
