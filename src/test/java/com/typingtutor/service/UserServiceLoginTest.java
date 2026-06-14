package com.typingtutor.service;

import com.typingtutor.dto.AuthResponse;
import com.typingtutor.dto.LoginRequest;
import com.typingtutor.entity.Role;
import com.typingtutor.entity.User;
import com.typingtutor.repository.UserPerformanceRepository;
import com.typingtutor.repository.UserRepository;
import com.typingtutor.security.JwtUtil;
import com.typingtutor.service.AuditLogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Regression tests for the no-email login fix.
 *
 * Bug: users created without an email address were being blocked at login with
 * EMAIL_NOT_VERIFIED (same as users who have an email but haven't verified it).
 * Fix: both the email-verification check and the first-login OTP flow now only
 * activate when the user actually has a non-blank email address.
 */
@ExtendWith(MockitoExtension.class)
class UserServiceLoginTest {

    @Mock UserRepository userRepository;
    @Mock UserPerformanceRepository performanceRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock JwtUtil jwtUtil;
    @Mock AuthenticationManager authenticationManager;
    @Mock OtpService otpService;
    @Mock EmailService emailService;
    @Mock AuditLogService auditLogService;

    @InjectMocks UserService userService;

    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("pass");
        // lenient: not every test reaches generateToken (some throw early)
        lenient().when(jwtUtil.generateToken(any(), any())).thenReturn("fake-jwt");
    }

    // ── Regression: no-email users must not be blocked ────────────────────────

    @Test
    void login_noEmailUser_emptyString_returnsToken() {
        User user = activeUser(null, false, true); // email=null
        user.setEmail("");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        Object result = userService.login(loginRequest);

        assertThat(result).isInstanceOf(AuthResponse.class);
        assertThat(((AuthResponse) result).getToken()).isEqualTo("fake-jwt");
        verify(otpService, never()).createOtp(any(), any());
    }

    @Test
    void login_noEmailUser_nullEmail_returnsToken() {
        User user = activeUser(null, false, true);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        Object result = userService.login(loginRequest);

        assertThat(result).isInstanceOf(AuthResponse.class);
        verify(otpService, never()).createOtp(any(), any());
    }

    @Test
    void login_noEmailUser_passwordNotChanged_skipsOtpAndReturnsToken() {
        // No email + passwordChanged=false → can't send OTP, so skip and return token directly
        User user = activeUser(null, false, false); // passwordChanged=false
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        Object result = userService.login(loginRequest);

        assertThat(result).isInstanceOf(AuthResponse.class);
        verify(otpService, never()).createOtp(any(), any());
        verify(emailService, never()).sendOtp(any(), any(), any(), any());
    }

    // ── Email + unverified must still be blocked ──────────────────────────────

    @Test
    void login_userWithEmailUnverified_throwsEmailNotVerified() {
        User user = activeUser("user@example.com", false, true); // emailVerified=false
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> userService.login(loginRequest))
                .isInstanceOf(UserService.EmailNotVerifiedException.class)
                .hasMessage("EMAIL_NOT_VERIFIED");
    }

    // ── Email + verified must be allowed ──────────────────────────────────────

    @Test
    void login_userWithEmailVerified_returnsToken() {
        User user = activeUser("user@example.com", true, true); // emailVerified=true
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        Object result = userService.login(loginRequest);

        assertThat(result).isInstanceOf(AuthResponse.class);
    }

    // ── Admin bypass: unverified email on ADMIN role must be allowed ──────────

    @Test
    void login_adminWithEmailUnverified_returnsToken() {
        User user = activeUser("admin@example.com", false, true);
        user.setRole(Role.ADMIN);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        Object result = userService.login(loginRequest);

        assertThat(result).isInstanceOf(AuthResponse.class);
    }

    // ── Inactive user must still be blocked regardless of email ──────────────

    @Test
    void login_inactiveUser_throwsAccountInactive() {
        User user = activeUser("", false, true);
        user.setActive(false);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> userService.login(loginRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("ACCOUNT_INACTIVE");
    }

    // ── First-login OTP flow triggers only when user HAS an email ─────────────

    @Test
    void login_userWithEmailPasswordNotChanged_triggersOtpFlow() {
        User user = activeUser("user@example.com", true, false); // passwordChanged=false
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        com.typingtutor.entity.EmailVerification ev = new com.typingtutor.entity.EmailVerification();
        ev.setOtpCode("123456");
        when(otpService.createOtp(any(), any())).thenReturn(ev);
        when(emailService.isMailEnabled()).thenReturn(false);

        Object result = userService.login(loginRequest);

        assertThat(result).isInstanceOf(Map.class);
        assertThat(((Map<?, ?>) result).get("requiresPasswordChange")).isEqualTo(true);
        verify(emailService).sendOtp(eq("user@example.com"), eq("123456"), eq("FIRST_LOGIN"), any());
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private User activeUser(String email, boolean emailVerified, boolean passwordChanged) {
        User u = new User();
        u.setId(1L);
        u.setUsername("testuser");
        u.setEmail(email);
        u.setEmailVerified(emailVerified);
        u.setPasswordChanged(passwordChanged);
        u.setActive(true);
        u.setRole(Role.USER);
        return u;
    }
}
