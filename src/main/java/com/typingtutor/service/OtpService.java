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

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Component
public class OtpService {

    private static final Logger log = LoggerFactory.getLogger(OtpService.class);
    private static final int OTP_EXPIRY_MINUTES = 30;
    private static final int MAX_ATTEMPTS = 5;

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
        ev = verificationRepository.save(ev);
        log.debug("OTP created for userId={} purpose={}", userId, purpose);
        return ev;
    }

    @Transactional
    public boolean validateOtp(String email, String otp, String purposeStr) {
        VerificationPurpose purpose = VerificationPurpose.valueOf(purposeStr);
        List<EmailVerification> results = verificationRepository.findUnusedByEmail(email, purpose);
        if (results.isEmpty()) return false;
        EmailVerification ev = results.get(0);
        if (ev.getAttemptCount() >= MAX_ATTEMPTS) return false;
        ev.setAttemptCount(ev.getAttemptCount() + 1);
        verificationRepository.save(ev);
        return !ev.isUsed()
                && ev.getExpiresAt().isAfter(LocalDateTime.now())
                && MessageDigest.isEqual(ev.getOtpCode().getBytes(), otp.getBytes());
    }

    @Transactional
    public void markUsed(Long verificationId) {
        verificationRepository.findById(verificationId).ifPresent(ev -> {
            ev.setUsed(true);
            verificationRepository.save(ev);
        });
    }

    @Transactional
    public Optional<EmailVerification> findValidOtp(String email, String otp, String purposeStr) {
        VerificationPurpose purpose = VerificationPurpose.valueOf(purposeStr);
        List<EmailVerification> results = verificationRepository.findUnusedByEmail(email, purpose);
        if (results.isEmpty()) return Optional.empty();
        EmailVerification ev = results.get(0);
        if (ev.getAttemptCount() >= MAX_ATTEMPTS) return Optional.empty();
        ev.setAttemptCount(ev.getAttemptCount() + 1);
        verificationRepository.save(ev);
        boolean valid = !ev.isUsed()
                && ev.getExpiresAt().isAfter(LocalDateTime.now())
                && MessageDigest.isEqual(ev.getOtpCode().getBytes(), otp.getBytes());
        return valid ? Optional.of(ev) : Optional.empty();
    }
}
