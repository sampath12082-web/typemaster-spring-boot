package com.typingtutor.service;

import com.typingtutor.dto.CertificateDto;
import com.typingtutor.entity.Certificate;
import com.typingtutor.entity.ExamAttempt;
import com.typingtutor.entity.User;
import com.typingtutor.repository.CertificateRepository;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CertificateService {

    private static final Logger log = LoggerFactory.getLogger(CertificateService.class);
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("MMMM d, yyyy");

    private final CertificateRepository certificateRepository;
    private final EmailService emailService;

    public CertificateService(CertificateRepository certificateRepository, EmailService emailService) {
        this.certificateRepository = certificateRepository;
        this.emailService = emailService;
    }

    @Transactional
    public Certificate issueCertificate(User user, ExamAttempt attempt) {
        Certificate cert = new Certificate();
        cert.setUser(user);
        cert.setExamAttempt(attempt);
        cert.setDifficultyLevel(attempt.getExam().getDifficultyLevel());
        cert.setCertificateId(UUID.randomUUID().toString());

        byte[] pdf = generatePdf(user, attempt, cert.getCertificateId());
        cert.setPdfData(pdf);
        cert = certificateRepository.save(cert);

        // Email the certificate
        if (user.getEmail() != null && !user.getEmail().isBlank()) {
            emailService.sendCertificate(user.getEmail(), user.getUsername(),
                    attempt.getExam().getDifficultyLevel().name(), pdf);
        }
        log.info("Certificate issued: id={} user={} tier={}", cert.getCertificateId(),
                user.getUsername(), cert.getDifficultyLevel());
        return cert;
    }

    public List<CertificateDto> getUserCertificates(Long userId) {
        return certificateRepository.findByUserIdOrderByIssuedAtDesc(userId)
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    public CertificateDto getCertificateByPublicId(String certificateId) {
        return certificateRepository.findByCertificateId(certificateId)
                .map(this::toDto)
                .orElseThrow(() -> new NoSuchElementException("Certificate not found: " + certificateId));
    }

    public byte[] getCertificatePdf(String certificateId) {
        Certificate cert = certificateRepository.findByCertificateId(certificateId)
                .orElseThrow(() -> new NoSuchElementException("Certificate not found: " + certificateId));
        if (cert.getPdfData() == null) {
            throw new IllegalArgumentException("PDF not available for certificate: " + certificateId);
        }
        return cert.getPdfData();
    }

    private byte[] generatePdf(User user, ExamAttempt attempt, String certId) {
        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            doc.addPage(page);

            float w = page.getMediaBox().getWidth();
            float h = page.getMediaBox().getHeight();
            String tier = attempt.getExam().getDifficultyLevel().name();
            String recipientName = (user.getFullName() != null && !user.getFullName().isBlank())
                    ? user.getFullName() : user.getUsername();
            String dateStr = attempt.getCompletedAt().format(DATE_FMT);

            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                // Background border
                cs.setNonStrokingColor(0.97f, 0.97f, 1.0f);
                cs.addRect(0, 0, w, h);
                cs.fill();

                // Title
                cs.beginText();
                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 28);
                cs.setNonStrokingColor(0.31f, 0.27f, 0.9f);
                String title = "TypeMaster";
                float titleWidth = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD).getStringWidth(title) / 1000 * 28;
                cs.newLineAtOffset((w - titleWidth) / 2, h - 80);
                cs.showText(title);
                cs.endText();

                // Subtitle
                cs.beginText();
                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 14);
                cs.setNonStrokingColor(0.4f, 0.4f, 0.4f);
                String sub = "Certificate of Achievement";
                float subWidth = new PDType1Font(Standard14Fonts.FontName.HELVETICA).getStringWidth(sub) / 1000 * 14;
                cs.newLineAtOffset((w - subWidth) / 2, h - 115);
                cs.showText(sub);
                cs.endText();

                // "This certifies that"
                cs.beginText();
                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 13);
                cs.setNonStrokingColor(0.3f, 0.3f, 0.3f);
                String certifies = "This certifies that";
                float certWidth = new PDType1Font(Standard14Fonts.FontName.HELVETICA).getStringWidth(certifies) / 1000 * 13;
                cs.newLineAtOffset((w - certWidth) / 2, h - 190);
                cs.showText(certifies);
                cs.endText();

                // Recipient name
                cs.beginText();
                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD_OBLIQUE), 22);
                cs.setNonStrokingColor(0.1f, 0.1f, 0.1f);
                float nameWidth = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD_OBLIQUE).getStringWidth(recipientName) / 1000 * 22;
                cs.newLineAtOffset((w - nameWidth) / 2, h - 230);
                cs.showText(recipientName);
                cs.endText();

                // Achievement text
                cs.beginText();
                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 13);
                cs.setNonStrokingColor(0.3f, 0.3f, 0.3f);
                String achieved = "has successfully completed the " + tier + " Typing Exam";
                float achWidth = new PDType1Font(Standard14Fonts.FontName.HELVETICA).getStringWidth(achieved) / 1000 * 13;
                cs.newLineAtOffset((w - achWidth) / 2, h - 270);
                cs.showText(achieved);
                cs.endText();

                // Stats
                cs.beginText();
                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 15);
                cs.setNonStrokingColor(0.31f, 0.27f, 0.9f);
                String stats = attempt.getWpm() + " WPM  |  " + String.format("%.1f", attempt.getAccuracy()) + "% Accuracy";
                float statsWidth = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD).getStringWidth(stats) / 1000 * 15;
                cs.newLineAtOffset((w - statsWidth) / 2, h - 315);
                cs.showText(stats);
                cs.endText();

                // Date
                cs.beginText();
                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 11);
                cs.setNonStrokingColor(0.5f, 0.5f, 0.5f);
                String dateLabel = "Issued: " + dateStr;
                float dateWidth = new PDType1Font(Standard14Fonts.FontName.HELVETICA).getStringWidth(dateLabel) / 1000 * 11;
                cs.newLineAtOffset((w - dateWidth) / 2, h - 360);
                cs.showText(dateLabel);
                cs.endText();

                // Certificate ID
                cs.beginText();
                cs.setFont(new PDType1Font(Standard14Fonts.FontName.COURIER), 8);
                cs.setNonStrokingColor(0.7f, 0.7f, 0.7f);
                String idLabel = "Certificate ID: " + certId;
                float idWidth = new PDType1Font(Standard14Fonts.FontName.COURIER).getStringWidth(idLabel) / 1000 * 8;
                cs.newLineAtOffset((w - idWidth) / 2, 30);
                cs.showText(idLabel);
                cs.endText();
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            doc.save(out);
            return out.toByteArray();
        } catch (Exception e) {
            log.error("PDF generation failed for certId={}: {}", certId, e.getMessage());
            return new byte[0];
        }
    }

    private CertificateDto toDto(Certificate cert) {
        CertificateDto dto = new CertificateDto();
        dto.setId(cert.getId());
        dto.setCertificateId(cert.getCertificateId());
        dto.setDifficultyLevel(cert.getDifficultyLevel().name());
        dto.setRecipientName(cert.getUser().getFullName() != null && !cert.getUser().getFullName().isBlank()
                ? cert.getUser().getFullName() : cert.getUser().getUsername());
        dto.setWpm(cert.getExamAttempt().getWpm());
        dto.setAccuracy(cert.getExamAttempt().getAccuracy());
        dto.setIssuedAt(cert.getIssuedAt().toString());
        return dto;
    }
}
