package com.typingtutor.service;

import com.typingtutor.entity.EmailVerification;
import com.typingtutor.entity.User;
import com.typingtutor.entity.VerificationPurpose;
import com.typingtutor.repository.EmailVerificationRepository;
import com.typingtutor.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import java.util.Optional;

@Component
public class OtpService {

    private static final Logger log = LoggerFactory.getLogger(OtpService.class);
    private static final int OTP_EXPIRY_MINUTES = 30;

    private final EmailVerificationRepository verificationRepository;
    private final UserRepository userRepository;

    public OtpService(EmailVerificationRepository verificationRepository,
                      UserRepository userRepository) {
        this.verificationRepository = verificationRepository;
        this.userRepository = userRepository;
    }

    public String generateOtp() {
        SecureRandom random = new SecureRandom();
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }

    @Transactional
    public EmailVerification createOtp(Long userId, String purposeStr) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found: " + userId));
        VerificationPurpose purpose = VerificationPurpose.valueOf(purposeStr);

        EmailVerification ev = new EmailVerification();
        ev.setUser(user);
        ev.setOtpCode(generateOtp());
        ev.setPurpose(purpose);
        ev.setExpiresAt(LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES));
        ev.setUsed(false);
        ev.setCreatedAt(LocalDateTime.now());
        ev = verificationRepository.save(ev);
        log.debug("OTP created for userId={} purpose={} code={}", userId, purpose, ev.getOtpCode());
        return ev;
    }

    public boolean validateOtp(String email, String otp, String purposeStr) {
        VerificationPurpose purpose = VerificationPurpose.valueOf(purposeStr);
        Optional<EmailVerification> opt = verificationRepository.findLatestUnusedByEmail(email, purpose);
        if (opt.isEmpty()) return false;
        EmailVerification ev = opt.get();
        return !ev.isUsed()
                && ev.getExpiresAt().isAfter(LocalDateTime.now())
                && ev.getOtpCode().equals(otp);
    }

    @Transactional
    public void markUsed(Long verificationId) {
        verificationRepository.findById(verificationId).ifPresent(ev -> {
            ev.setUsed(true);
            verificationRepository.save(ev);
        });
    }

    public Optional<EmailVerification> findValidOtp(String email, String otp, String purposeStr) {
        VerificationPurpose purpose = VerificationPurpose.valueOf(purposeStr);
        return verificationRepository.findLatestUnusedByEmail(email, purpose)
                .filter(ev -> !ev.isUsed()
                        && ev.getExpiresAt().isAfter(LocalDateTime.now())
                        && ev.getOtpCode().equals(otp));
    }
}
