package com.typingtutor.service;

import com.typingtutor.entity.Role;
import com.typingtutor.entity.User;
import com.typingtutor.repository.UserPerformanceRepository;
import com.typingtutor.repository.UserRepository;
import com.typingtutor.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceUpdatePasswordTest {

    @Mock UserRepository userRepository;
    @Mock UserPerformanceRepository performanceRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock JwtUtil jwtUtil;
    @Mock AuthenticationManager authenticationManager;
    @Mock OtpService otpService;
    @Mock EmailService emailService;
    @Mock AuditLogService auditLogService;

    @InjectMocks UserService userService;

    private User user;
    private static final String STORED_HASH   = "hashed-current-password";
    private static final String CURRENT_PASS  = "Current@1234";
    private static final String NEW_PASS      = "New@5678!";

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setPassword(STORED_HASH);
        user.setActive(true);
        user.setRole(Role.USER);
    }

    @Test
    void updatePassword_correctCurrentPassword_savesEncodedNewPassword() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(CURRENT_PASS, STORED_HASH)).thenReturn(true);
        when(passwordEncoder.matches(NEW_PASS, STORED_HASH)).thenReturn(false);
        when(passwordEncoder.encode(NEW_PASS)).thenReturn("hashed-new-password");
        when(userRepository.save(any(User.class))).thenReturn(user);

        userService.updatePassword("testuser", CURRENT_PASS, NEW_PASS);

        verify(passwordEncoder).encode(NEW_PASS);
        verify(userRepository).save(user);
        assertThat(user.getPassword()).isEqualTo("hashed-new-password");
    }

    @Test
    void updatePassword_wrongCurrentPassword_throwsIllegalArgument() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Wrong@Pass1", STORED_HASH)).thenReturn(false);

        assertThatThrownBy(() ->
                userService.updatePassword("testuser", "Wrong@Pass1", NEW_PASS))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Current password is incorrect.");

        verify(userRepository, never()).save(any());
    }

    @Test
    void updatePassword_newPasswordSameAsCurrent_throwsIllegalArgument() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        // Both the current-check and the same-password-check receive the same args → both true
        when(passwordEncoder.matches(CURRENT_PASS, STORED_HASH)).thenReturn(true);

        assertThatThrownBy(() ->
                userService.updatePassword("testuser", CURRENT_PASS, CURRENT_PASS))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("New password must be different from your current password.");

        verify(userRepository, never()).save(any());
    }

    @Test
    void updatePassword_userNotFound_throwsNoSuchElement() {
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                userService.updatePassword("ghost", CURRENT_PASS, NEW_PASS))
                .isInstanceOf(NoSuchElementException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    void updatePassword_success_writesAuditLog() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(CURRENT_PASS, STORED_HASH)).thenReturn(true);
        when(passwordEncoder.matches(NEW_PASS, STORED_HASH)).thenReturn(false);
        when(passwordEncoder.encode(NEW_PASS)).thenReturn("hashed-new-password");
        when(userRepository.save(any())).thenReturn(user);

        userService.updatePassword("testuser", CURRENT_PASS, NEW_PASS);

        verify(auditLogService).log(eq("testuser"), eq("PASSWORD_UPDATED"), anyString());
    }

    @Test
    void updatePassword_success_doesNotCallOtpOrEmail() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(CURRENT_PASS, STORED_HASH)).thenReturn(true);
        when(passwordEncoder.matches(NEW_PASS, STORED_HASH)).thenReturn(false);
        when(passwordEncoder.encode(NEW_PASS)).thenReturn("hashed-new-password");
        when(userRepository.save(any())).thenReturn(user);

        userService.updatePassword("testuser", CURRENT_PASS, NEW_PASS);

        verify(otpService, never()).createOtp(any(), any());
        verify(emailService, never()).sendOtp(any(), any(), any(), any());
    }
}
