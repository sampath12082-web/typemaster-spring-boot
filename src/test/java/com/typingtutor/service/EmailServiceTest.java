package com.typingtutor.service;

import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Covers B-7: sendOtp() must report success/failure rather than silently swallowing a send
 * failure and letting the caller believe the email went out.
 */
@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock JavaMailSender mailSender;

    @Test
    void sendOtp_mailDisabled_returnsFalseWithoutThrowing() {
        EmailService service = new EmailService(null, "");

        boolean sent = service.sendOtp("user@example.com", "123456", "VERIFY_EMAIL", "user");

        assertThat(sent).isFalse();
    }

    // MimeMessageHelper does real work against the MimeMessage (setRecipients, setText, etc.) —
    // a bare Mockito mock NPEs deep inside javax.mail internals before send() is ever reached.
    // Use a real MimeMessage backed by a throwaway Session instead.
    private static MimeMessage realMimeMessage() {
        return new MimeMessage(Session.getInstance(new java.util.Properties()));
    }

    @Test
    void sendOtp_mailEnabledAndSendSucceeds_returnsTrue() {
        EmailService service = new EmailService(mailSender, "noreply@typemaster.app");
        when(mailSender.createMimeMessage()).thenReturn(realMimeMessage());

        boolean sent = service.sendOtp("user@example.com", "123456", "VERIFY_EMAIL", "user");

        assertThat(sent).isTrue();
        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    void sendOtp_mailEnabledButSendThrows_returnsFalseInsteadOfPropagating() {
        EmailService service = new EmailService(mailSender, "noreply@typemaster.app");
        when(mailSender.createMimeMessage()).thenReturn(realMimeMessage());
        doThrow(new org.springframework.mail.MailSendException("SMTP auth failed"))
                .when(mailSender).send(any(MimeMessage.class));

        boolean sent = service.sendOtp("user@example.com", "123456", "VERIFY_EMAIL", "user");

        assertThat(sent).isFalse();
    }

    @Test
    void isMailEnabled_falseWhenUsernameBlank() {
        EmailService service = new EmailService(mailSender, "  ");
        assertThat(service.isMailEnabled()).isFalse();
    }

    @Test
    void isMailEnabled_falseWhenMailSenderNull() {
        EmailService service = new EmailService(null, "noreply@typemaster.app");
        assertThat(service.isMailEnabled()).isFalse();
    }

    @Test
    void isMailEnabled_trueWhenBothPresent() {
        EmailService service = new EmailService(mailSender, "noreply@typemaster.app");
        assertThat(service.isMailEnabled()).isTrue();
    }
}
