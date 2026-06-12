package com.typingtutor.service;

import com.typingtutor.entity.EmailVerification;
import com.typingtutor.entity.User;
import com.typingtutor.entity.VerificationPurpose;
import com.typingtutor.repository.EmailVerificationRepository;
import com.typingtutor.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OtpServiceTest {

    @Mock EmailVerificationRepository verificationRepository;
    @Mock UserRepository userRepository;

    @InjectMocks OtpService otpService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@example.com");
    }

    @Test
    void generateOtpProducesSixDigitCode() {
        String otp = otpService.generateOtp();
        assertThat(otp).hasSize(6).matches("\\d{6}");
    }

    @Test
    void generateOtpIsRandom() {
        String otp1 = otpService.generateOtp();
        String otp2 = otpService.generateOtp();
        // Extremely unlikely to be equal
        assertThat(otp1).isNotEqualTo(otp2);
    }

    @Test
    void createOtpSavesVerificationRecord() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        EmailVerification saved = new EmailVerification();
        saved.setId(100L);
        saved.setUser(user);
        saved.setOtpCode("123456");
        saved.setPurpose(VerificationPurpose.VERIFY_EMAIL);
        saved.setExpiresAt(LocalDateTime.now().plusMinutes(30));
        when(verificationRepository.save(any())).thenReturn(saved);

        EmailVerification result = otpService.createOtp(1L, "VERIFY_EMAIL");

        ArgumentCaptor<EmailVerification> captor = ArgumentCaptor.forClass(EmailVerification.class);
        verify(verificationRepository).save(captor.capture());
        EmailVerification captured = captor.getValue();
        assertThat(captured.getPurpose()).isEqualTo(VerificationPurpose.VERIFY_EMAIL);
        assertThat(captured.isUsed()).isFalse();
        assertThat(captured.getExpiresAt()).isAfter(LocalDateTime.now());
        assertThat(result.getId()).isEqualTo(100L);
    }

    @Test
    void validateOtpReturnsFalseWhenNoRecord() {
        when(verificationRepository.findUnusedByEmail("test@example.com", VerificationPurpose.VERIFY_EMAIL))
                .thenReturn(List.of());

        boolean valid = otpService.validateOtp("test@example.com", "123456", "VERIFY_EMAIL");

        assertThat(valid).isFalse();
    }

    @Test
    void validateOtpReturnsFalseForExpiredCode() {
        EmailVerification ev = new EmailVerification();
        ev.setUser(user);
        ev.setOtpCode("999999");
        ev.setUsed(false);
        ev.setAttemptCount(0);
        ev.setExpiresAt(LocalDateTime.now().minusMinutes(1)); // expired
        when(verificationRepository.save(any())).thenReturn(ev);

        when(verificationRepository.findUnusedByEmail("test@example.com", VerificationPurpose.VERIFY_EMAIL))
                .thenReturn(List.of(ev));

        boolean valid = otpService.validateOtp("test@example.com", "999999", "VERIFY_EMAIL");

        assertThat(valid).isFalse();
    }

    @Test
    void validateOtpReturnsTrueForValidCode() {
        EmailVerification ev = new EmailVerification();
        ev.setUser(user);
        ev.setOtpCode("654321");
        ev.setUsed(false);
        ev.setAttemptCount(0);
        ev.setExpiresAt(LocalDateTime.now().plusMinutes(25));
        when(verificationRepository.save(any())).thenReturn(ev);

        when(verificationRepository.findUnusedByEmail("test@example.com", VerificationPurpose.VERIFY_EMAIL))
                .thenReturn(List.of(ev));

        boolean valid = otpService.validateOtp("test@example.com", "654321", "VERIFY_EMAIL");

        assertThat(valid).isTrue();
    }

    @Test
    void markUsedSetsUsedFlag() {
        EmailVerification ev = new EmailVerification();
        ev.setId(5L);
        ev.setUsed(false);
        when(verificationRepository.findById(5L)).thenReturn(Optional.of(ev));

        otpService.markUsed(5L);

        ArgumentCaptor<EmailVerification> captor = ArgumentCaptor.forClass(EmailVerification.class);
        verify(verificationRepository).save(captor.capture());
        assertThat(captor.getValue().isUsed()).isTrue();
    }
}
