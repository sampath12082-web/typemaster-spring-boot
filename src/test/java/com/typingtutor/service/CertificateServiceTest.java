package com.typingtutor.service;

import com.typingtutor.dto.CertificateDto;
import com.typingtutor.entity.*;
import com.typingtutor.repository.CertificateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CertificateServiceTest {

    @Mock CertificateRepository certificateRepository;
    @Mock EmailService emailService;

    @InjectMocks CertificateService certificateService;

    private User user;
    private Exam exam;
    private ExamAttempt attempt;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("student");
        user.setFullName("John Doe");
        user.setEmail("student@example.com");

        exam = new Exam();
        exam.setId(3L);
        exam.setDifficultyLevel(DifficultyLevel.ADVANCED);
        exam.setDurationMinutes(60);
        exam.setMinWpm(55);
        exam.setMinAccuracy(90.0);
        exam.setContentText("exam content");

        attempt = new ExamAttempt();
        attempt.setId(99L);
        attempt.setUser(user);
        attempt.setExam(exam);
        attempt.setWpm(65);
        attempt.setAccuracy(94.5);
        attempt.setPassed(true);
        attempt.setStartedAt(LocalDateTime.of(2026, 6, 1, 10, 0));
        attempt.setCompletedAt(LocalDateTime.of(2026, 6, 1, 11, 0));
    }

    // ── issueCertificate ────────────────────────────────────────────────────

    @Test
    void issueCertificate_savesWithCorrectFields() {
        when(certificateRepository.save(any(Certificate.class))).thenAnswer(inv -> {
            Certificate c = inv.getArgument(0);
            c.setId(10L);
            c.setIssuedAt(LocalDateTime.now());
            return c;
        });

        Certificate result = certificateService.issueCertificate(user, attempt);

        ArgumentCaptor<Certificate> captor = ArgumentCaptor.forClass(Certificate.class);
        verify(certificateRepository).save(captor.capture());

        Certificate saved = captor.getValue();
        assertThat(saved.getUser()).isSameAs(user);
        assertThat(saved.getExamAttempt()).isSameAs(attempt);
        assertThat(saved.getDifficultyLevel()).isEqualTo(DifficultyLevel.ADVANCED);
        assertThat(saved.getCertificateId()).isNotBlank();
        assertThat(saved.getPdfData()).isNotNull();
        assertThat(saved.getPdfData().length).isGreaterThan(0);

        assertThat(result).isSameAs(saved);
    }

    @Test
    void issueCertificate_sendsEmailWhenUserHasEmail() {
        when(certificateRepository.save(any(Certificate.class))).thenAnswer(inv -> {
            Certificate c = inv.getArgument(0);
            c.setId(10L);
            c.setIssuedAt(LocalDateTime.now());
            return c;
        });

        certificateService.issueCertificate(user, attempt);

        verify(emailService).sendCertificate(
                eq("student@example.com"),
                eq("John Doe"),
                eq("ADVANCED"),
                any(byte[].class)
        );
    }

    @Test
    void issueCertificate_usesUsernameWhenFullNameBlank() {
        user.setFullName("");
        when(certificateRepository.save(any(Certificate.class))).thenAnswer(inv -> {
            Certificate c = inv.getArgument(0);
            c.setId(10L);
            c.setIssuedAt(LocalDateTime.now());
            return c;
        });

        certificateService.issueCertificate(user, attempt);

        verify(emailService).sendCertificate(
                eq("student@example.com"),
                eq("student"),
                eq("ADVANCED"),
                any(byte[].class)
        );
    }

    @Test
    void issueCertificate_skipsEmailWhenEmailNull() {
        user.setEmail(null);
        when(certificateRepository.save(any(Certificate.class))).thenAnswer(inv -> {
            Certificate c = inv.getArgument(0);
            c.setId(10L);
            c.setIssuedAt(LocalDateTime.now());
            return c;
        });

        certificateService.issueCertificate(user, attempt);

        verify(emailService, never()).sendCertificate(any(), any(), any(), any());
    }

    @Test
    void issueCertificate_skipsEmailWhenEmailBlank() {
        user.setEmail("   ");
        when(certificateRepository.save(any(Certificate.class))).thenAnswer(inv -> {
            Certificate c = inv.getArgument(0);
            c.setId(10L);
            c.setIssuedAt(LocalDateTime.now());
            return c;
        });

        certificateService.issueCertificate(user, attempt);

        verify(emailService, never()).sendCertificate(any(), any(), any(), any());
    }

    // ── getUserCertificates ─────────────────────────────────────────────────

    @Test
    void getUserCertificates_returnsDtoList() {
        Certificate cert = buildCertificate(1L, "cert-abc-123", DifficultyLevel.INTERMEDIATE);

        when(certificateRepository.findByUserIdOrderByIssuedAtDesc(1L)).thenReturn(List.of(cert));

        List<CertificateDto> result = certificateService.getUserCertificates(1L);

        assertThat(result).hasSize(1);
        CertificateDto dto = result.get(0);
        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getCertificateId()).isEqualTo("cert-abc-123");
        assertThat(dto.getDifficultyLevel()).isEqualTo("INTERMEDIATE");
        assertThat(dto.getRecipientName()).isEqualTo("John Doe");
        assertThat(dto.getWpm()).isEqualTo(65);
        assertThat(dto.getAccuracy()).isEqualTo(94.5);
        assertThat(dto.getIssuedAt()).isNotBlank();
    }

    @Test
    void getUserCertificates_returnsEmptyListWhenNoCertificates() {
        when(certificateRepository.findByUserIdOrderByIssuedAtDesc(1L)).thenReturn(List.of());

        List<CertificateDto> result = certificateService.getUserCertificates(1L);

        assertThat(result).isEmpty();
    }

    // ── getCertificateByPublicId ────────────────────────────────────────────

    @Test
    void getCertificateByPublicId_returnsDtoWhenFound() {
        Certificate cert = buildCertificate(5L, "pub-id-456", DifficultyLevel.BASIC);

        when(certificateRepository.findByCertificateId("pub-id-456")).thenReturn(Optional.of(cert));

        CertificateDto dto = certificateService.getCertificateByPublicId("pub-id-456");

        assertThat(dto.getCertificateId()).isEqualTo("pub-id-456");
        assertThat(dto.getDifficultyLevel()).isEqualTo("BASIC");
        assertThat(dto.getRecipientName()).isEqualTo("John Doe");
        assertThat(dto.getWpm()).isEqualTo(65);
        assertThat(dto.getAccuracy()).isEqualTo(94.5);
    }

    @Test
    void getCertificateByPublicId_throwsWhenNotFound() {
        when(certificateRepository.findByCertificateId("missing-id")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> certificateService.getCertificateByPublicId("missing-id"))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("Certificate not found");
    }

    // ── getCertificatePdf ───────────────────────────────────────────────────

    @Test
    void getCertificatePdf_returnsPdfDataWhenPresent() {
        byte[] pdfBytes = new byte[]{1, 2, 3, 4, 5};
        Certificate cert = buildCertificate(7L, "pdf-cert-789", DifficultyLevel.ADVANCED);
        cert.setPdfData(pdfBytes);

        when(certificateRepository.findByCertificateId("pdf-cert-789")).thenReturn(Optional.of(cert));

        byte[] result = certificateService.getCertificatePdf("pdf-cert-789");

        assertThat(result).isEqualTo(pdfBytes);
        // Should not re-save when PDF data already exists
        verify(certificateRepository, never()).save(any());
    }

    @Test
    void getCertificatePdf_regeneratesWhenPdfDataNull() {
        Certificate cert = buildCertificate(8L, "regen-cert", DifficultyLevel.INTERMEDIATE);
        cert.setPdfData(null);

        when(certificateRepository.findByCertificateId("regen-cert")).thenReturn(Optional.of(cert));
        when(certificateRepository.save(any(Certificate.class))).thenAnswer(inv -> inv.getArgument(0));

        byte[] result = certificateService.getCertificatePdf("regen-cert");

        assertThat(result).isNotNull();
        assertThat(result.length).isGreaterThan(0);
        verify(certificateRepository).save(cert);
    }

    @Test
    void getCertificatePdf_throwsWhenNotFound() {
        when(certificateRepository.findByCertificateId("no-such")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> certificateService.getCertificatePdf("no-such"))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("Certificate not found");
    }

    // ── helpers ─────────────────────────────────────────────────────────────

    private Certificate buildCertificate(Long id, String certId, DifficultyLevel level) {
        Certificate cert = new Certificate();
        cert.setId(id);
        cert.setCertificateId(certId);
        cert.setDifficultyLevel(level);
        cert.setUser(user);
        cert.setExamAttempt(attempt);
        cert.setIssuedAt(LocalDateTime.of(2026, 6, 1, 12, 0));
        return cert;
    }
}
