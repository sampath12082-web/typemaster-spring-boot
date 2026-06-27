package com.typingtutor.service;

import com.typingtutor.dto.AdminUserDto;
import com.typingtutor.dto.InquiryDto;
import com.typingtutor.entity.*;
import com.typingtutor.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock UserRepository userRepository;
    @Mock UserPerformanceRepository performanceRepository;
    @Mock InquiryRepository inquiryRepository;
    @Mock ExamAttemptRepository examAttemptRepository;
    @Mock CertificateRepository certificateRepository;
    @Mock EmailVerificationRepository emailVerificationRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock AuditLogService auditLogService;
    @Mock OtpService otpService;
    @Mock EmailService emailService;

    @InjectMocks AdminService adminService;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .username("testuser")
                .email("test@example.com")
                .password("encoded-password")
                .role(Role.USER)
                .active(true)
                .emailVerified(true)
                .build();
        user.setId(1L);
    }

    // --- createUser ---

    @Test
    void createUser_success() {
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(passwordEncoder.encode("rawPassword")).thenReturn("encoded-pass");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User saved = invocation.getArgument(0);
            saved.setId(42L);
            return saved;
        });

        AdminUserDto result = adminService.createUser("newuser", "new@example.com", "rawPassword", "admin");

        assertThat(result.getId()).isEqualTo(42L);
        assertThat(result.getUsername()).isEqualTo("newuser");
        assertThat(result.getEmail()).isEqualTo("new@example.com");
        assertThat(result.getRole()).isEqualTo("USER");

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User savedUser = captor.getValue();
        assertThat(savedUser.getPassword()).isEqualTo("encoded-pass");
        assertThat(savedUser.isEmailVerified()).isTrue();
        assertThat(savedUser.isPasswordChanged()).isFalse();
        assertThat(savedUser.isPlacementCompleted()).isTrue();

        verify(auditLogService).log(eq("admin"), eq("USER_CREATE"), anyString());
    }

    @Test
    void createUser_duplicateUsername_throws() {
        when(userRepository.existsByUsername("taken")).thenReturn(true);

        assertThatThrownBy(() -> adminService.createUser("taken", "x@example.com", "pass", "admin"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Username already taken");
    }

    @Test
    void createUser_duplicateEmail_throws() {
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("taken@example.com")).thenReturn(true);

        assertThatThrownBy(() -> adminService.createUser("newuser", "taken@example.com", "pass", "admin"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Email already registered");
    }

    // --- deleteUser ---

    @Test
    void deleteUser_deletesInFkCascadeOrder() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        adminService.deleteUser(1L, "admin");

        // Verify FK-safe deletion order
        var order = inOrder(certificateRepository, examAttemptRepository,
                performanceRepository, inquiryRepository, emailVerificationRepository, userRepository);
        order.verify(certificateRepository).deleteByUserId(1L);
        order.verify(examAttemptRepository).deleteByUserId(1L);
        order.verify(performanceRepository).deleteByUserId(1L);
        order.verify(inquiryRepository).deleteByUserId(1L);
        order.verify(emailVerificationRepository).deleteByUserId(1L);
        order.verify(userRepository).deleteById(1L);

        verify(auditLogService).log(eq("admin"), eq("USER_DELETE"), anyString());
    }

    @Test
    void deleteUser_notFound_throws() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminService.deleteUser(99L, "admin"))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("User not found");
    }

    // --- toggleActive ---

    @Test
    void toggleActive_deactivatesActiveUser() {
        user.setActive(true);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        boolean result = adminService.toggleActive(1L, "admin");

        assertThat(result).isFalse();
        assertThat(user.isActive()).isFalse();
        verify(auditLogService).log(eq("admin"), eq("USER_DEACTIVATE"), anyString());
    }

    @Test
    void toggleActive_activatesInactiveUser() {
        user.setActive(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        boolean result = adminService.toggleActive(1L, "admin");

        assertThat(result).isTrue();
        assertThat(user.isActive()).isTrue();
        verify(auditLogService).log(eq("admin"), eq("USER_ACTIVATE"), anyString());
    }

    @Test
    void toggleActive_notFound_throws() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminService.toggleActive(99L, "admin"))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("User not found");
    }

    // --- resetPassword ---

    @Test
    void resetPassword_withEmail_sendsOtp() {
        user.setEmail("test@example.com");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        EmailVerification ev = new EmailVerification();
        ev.setOtpCode("123456");
        when(otpService.createOtp(1L, "RESET_PASSWORD")).thenReturn(ev);
        when(emailService.sendOtp(eq("test@example.com"), eq("123456"), eq("RESET_PASSWORD"), anyString()))
                .thenReturn(true);

        Map<String, Object> result = adminService.resetPassword(1L, "admin");

        assertThat(result.get("otpSent")).isEqualTo(true);
        assertThat(result.get("message")).asString().contains("OTP sent");
        verify(otpService).createOtp(1L, "RESET_PASSWORD");
        verify(emailService).sendOtp(eq("test@example.com"), eq("123456"), eq("RESET_PASSWORD"), anyString());
        verify(auditLogService).log(eq("admin"), eq("PASSWORD_RESET_OTP"), anyString());
    }

    @Test
    void resetPassword_withoutEmail_generatesTempPassword() {
        user.setEmail(null);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode(anyString())).thenReturn("encoded-temp");

        Map<String, Object> result = adminService.resetPassword(1L, "admin");

        assertThat(result.get("temporaryPassword")).isNotNull();
        String tempPassword = (String) result.get("temporaryPassword");
        assertThat(tempPassword).hasSize(16);
        assertThat(result.get("message")).asString().contains("Temporary password");

        verify(userRepository).save(user);
        verify(passwordEncoder).encode(tempPassword);
        verify(auditLogService).log(eq("admin"), eq("PASSWORD_RESET"), anyString());
        // OTP should not be called
        verifyNoInteractions(otpService);
    }

    @Test
    void resetPassword_withBlankEmail_generatesTempPassword() {
        user.setEmail("   ");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode(anyString())).thenReturn("encoded-temp");

        Map<String, Object> result = adminService.resetPassword(1L, "admin");

        assertThat(result.get("temporaryPassword")).isNotNull();
        String tempPassword = (String) result.get("temporaryPassword");
        assertThat(tempPassword).hasSize(16);
        verifyNoInteractions(otpService);
    }

    @Test
    void resetPassword_notFound_throws() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminService.resetPassword(99L, "admin"))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("User not found");
    }

    // --- getAllUsers ---

    @Test
    void getAllUsers_returnsNonAdminUsersWithStats() {
        User admin = User.builder().username("admin").role(Role.ADMIN).password("x").build();
        admin.setId(99L);

        User student = User.builder().username("student").role(Role.USER).password("x").email("s@e.com").build();
        student.setId(2L);

        when(userRepository.findAll()).thenReturn(List.of(admin, student));

        // Create performance records for the student
        Lesson lesson = new Lesson();
        lesson.setId(1L);
        lesson.setTitle("Home Row");
        lesson.setDifficultyLevel(DifficultyLevel.BASIC);

        UserPerformance perf1 = UserPerformance.builder()
                .user(student).lesson(lesson).wpm(45).accuracyPercentage(92.0).build();
        UserPerformance perf2 = UserPerformance.builder()
                .user(student).lesson(lesson).wpm(55).accuracyPercentage(88.0).build();

        when(performanceRepository.findAllWithUser()).thenReturn(List.of(perf1, perf2));

        List<AdminUserDto> result = adminService.getAllUsers();

        // Admin user should be filtered out
        assertThat(result).hasSize(1);
        AdminUserDto dto = result.get(0);
        assertThat(dto.getUsername()).isEqualTo("student");
        assertThat(dto.getTotalRuns()).isEqualTo(2);
        assertThat(dto.getAvgWpm()).isEqualTo(50.0);
        assertThat(dto.getBestWpm()).isEqualTo(55);
    }

    @Test
    void getAllUsers_noPerformance_returnsZeroStats() {
        User student = User.builder().username("newbie").role(Role.USER).password("x").build();
        student.setId(3L);

        when(userRepository.findAll()).thenReturn(List.of(student));
        when(performanceRepository.findAllWithUser()).thenReturn(Collections.emptyList());

        List<AdminUserDto> result = adminService.getAllUsers();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTotalRuns()).isZero();
        assertThat(result.get(0).getAvgWpm()).isZero();
        assertThat(result.get(0).getBestWpm()).isZero();
    }

    // --- resolveInquiry ---

    @Test
    void resolveInquiry_setsStatusAndAuditLog() {
        Inquiry inquiry = new Inquiry();
        inquiry.setId(10L);
        inquiry.setSubject("Help me");
        inquiry.setMessage("I need help");
        inquiry.setStatus(InquiryStatus.OPEN);
        inquiry.setUser(user);
        inquiry.setCreatedAt(LocalDateTime.of(2026, 1, 15, 10, 30));

        when(inquiryRepository.findById(10L)).thenReturn(Optional.of(inquiry));
        when(inquiryRepository.save(any(Inquiry.class))).thenAnswer(inv -> inv.getArgument(0));

        InquiryDto result = adminService.resolveInquiry(10L, "Issue fixed", "admin");

        assertThat(result.getStatus()).isEqualTo("RESOLVED");
        assertThat(result.getAdminResponse()).isEqualTo("Issue fixed");
        assertThat(result.getPriority()).isEqualTo("NORMAL");

        ArgumentCaptor<Inquiry> captor = ArgumentCaptor.forClass(Inquiry.class);
        verify(inquiryRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(InquiryStatus.RESOLVED);
        assertThat(captor.getValue().getAdminResponse()).isEqualTo("Issue fixed");

        verify(auditLogService).log(eq("admin"), eq("INQUIRY_RESOLVED"), anyString());
    }

    @Test
    void resolveInquiry_notFound_throws() {
        when(inquiryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminService.resolveInquiry(99L, "response", "admin"))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("Inquiry not found");
    }
}
