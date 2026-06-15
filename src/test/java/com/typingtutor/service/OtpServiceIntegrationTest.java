package com.typingtutor.service;

import com.typingtutor.entity.EmailVerification;
import com.typingtutor.entity.User;
import com.typingtutor.repository.EmailVerificationRepository;
import com.typingtutor.repository.UserRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@SpringBootTest
@Transactional
class OtpServiceIntegrationTest {

    @Autowired
    OtpService otpService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    EmailVerificationRepository verificationRepository;

    @Test
    void exceedMaxAttemptsLocksOtp() {
        User user = new User();
        user.setUsername("int-test-user");
        user.setEmail("int-test@example.com");
        user = userRepository.save(user);
        String email = user.getEmail();

        EmailVerification ev = otpService.createOtp(user.getId(), "VERIFY_EMAIL");

        // Perform MAX_ATTEMPTS invalid attempts then expect the next call to throw
        int attempts = 6; // service MAX_ATTEMPTS is 5, so 6th should cause lock
        for (int i = 0; i < attempts - 1; i++) {
            Optional<EmailVerification> res = otpService.findValidOtp(email, "000000", "VERIFY_EMAIL");
            Assertions.assertThat(res).isEmpty();
        }

        // Next attempt should throw IllegalArgumentException indicating lock
        Assertions.assertThatThrownBy(() -> otpService.findValidOtp(email, "000000", "VERIFY_EMAIL"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("OTP locked");
    }
}
