package com.typingtutor.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;
    private final boolean mailEnabled;

    @Value("${spring.mail.username:}")
    private String fromAddress;

    public EmailService(@Autowired(required = false) JavaMailSender mailSender,
                        @Value("${spring.mail.username:}") String mailUsername) {
        this.mailSender = mailSender;
        this.mailEnabled = mailSender != null && !mailUsername.isBlank();
    }

    public boolean isMailEnabled() {
        return mailEnabled;
    }

    public void sendOtp(String toEmail, String otp, String purpose, String userName) {
        String subject = buildOtpSubject(purpose);
        String body = buildOtpBody(userName, otp, purpose);
        send(toEmail, subject, body);
    }

    public void sendWelcome(String toEmail, String userName) {
        String subject = "Welcome to TypeMaster!";
        String body = "<h2>Welcome, " + HtmlUtils.htmlEscape(userName) + "!</h2>"
                + "<p>Your account has been successfully created and verified.</p>"
                + "<p>Start practising to improve your typing speed and accuracy.</p>";
        send(toEmail, subject, body);
    }

    public void sendCertificate(String toEmail, String userName, String tier, byte[] pdfBytes) {
        if (!mailEnabled) {
            log.info("Mail disabled — certificate for {} ({}) not sent to {}", userName, tier, toEmail);
            return;
        }
        try {
            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, true, "UTF-8");
            helper.setFrom(fromAddress);
            helper.setTo(toEmail);
            helper.setSubject("Your TypeMaster " + tier + " Certificate");
            helper.setText("<p>Congratulations, " + HtmlUtils.htmlEscape(userName) + "!</p>"
                    + "<p>Please find your " + HtmlUtils.htmlEscape(tier) + " typing certificate attached.</p>", true);
            helper.addAttachment("TypeMaster_Certificate_" + tier + ".pdf",
                    () -> new java.io.ByteArrayInputStream(pdfBytes), "application/pdf");
            mailSender.send(msg);
            log.info("Certificate email sent to {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send certificate email to {}: {}", toEmail, e.getMessage());
        }
    }

    private void send(String toEmail, String subject, String htmlBody) {
        if (!mailEnabled) {
            log.info("Mail disabled — would send to={} subject='{}'", toEmail, subject);
            return;
        }
        try {
            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, true, "UTF-8");
            helper.setFrom(fromAddress);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            mailSender.send(msg);
            log.info("Email sent to {} — subject: {}", toEmail, subject);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", toEmail, e.getMessage());
        }
    }

    private String buildOtpSubject(String purpose) {
        return switch (purpose) {
            case "VERIFY_EMAIL"    -> "TypeMaster — Verify Your Email";
            case "FIRST_LOGIN"     -> "TypeMaster — Complete Your First Login";
            case "RESET_PASSWORD"  -> "TypeMaster — Password Reset OTP";
            default                -> "TypeMaster — Your OTP Code";
        };
    }

    private String buildOtpBody(String userName, String otp, String purpose) {
        String action = switch (purpose) {
            case "VERIFY_EMAIL"   -> "verify your email address";
            case "FIRST_LOGIN"    -> "complete your first login and set a new password";
            case "RESET_PASSWORD" -> "reset your password";
            default               -> "complete the requested action";
        };
        return "<h2>Hello, " + HtmlUtils.htmlEscape(userName) + "!</h2>"
                + "<p>Use the following OTP to " + action + ":</p>"
                + "<h1 style='letter-spacing:6px;color:#4F46E5'>" + otp + "</h1>"
                + "<p>This code expires in <strong>30 minutes</strong>.</p>"
                + "<p>If you did not request this, please ignore this email.</p>";
    }
}
