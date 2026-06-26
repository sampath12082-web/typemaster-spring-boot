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
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
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
            String displayName = (user.getFullName() != null && !user.getFullName().isBlank())
                    ? user.getFullName() : user.getUsername();
            emailService.sendCertificate(user.getEmail(), displayName,
                    attempt.getExam().getDifficultyLevel().name(), pdf);
        }
        log.info("Certificate issued: id={} user={} tier={}", cert.getCertificateId(),
                user.getUsername(), cert.getDifficultyLevel());
        return cert;
    }

    @Transactional(readOnly = true)
    public List<CertificateDto> getUserCertificates(Long userId) {
        return certificateRepository.findByUserIdOrderByIssuedAtDesc(userId)
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CertificateDto getCertificateByPublicId(String certificateId) {
        return certificateRepository.findByCertificateId(certificateId)
                .map(this::toPublicDto)
                .orElseThrow(() -> new NoSuchElementException("Certificate not found: " + certificateId));
    }

    @Transactional
    public byte[] getCertificatePdf(String certificateId) {
        Certificate cert = certificateRepository.findByCertificateId(certificateId)
                .orElseThrow(() -> new NoSuchElementException("Certificate not found: " + certificateId));
        if (cert.getPdfData() == null) {
            byte[] pdf = generatePdf(cert.getUser(), cert.getExamAttempt(), cert.getCertificateId());
            cert.setPdfData(pdf);
            certificateRepository.save(cert);
            log.info("PDF regenerated for migrated certificate: {}", certificateId);
            return pdf;
        }
        return cert.getPdfData();
    }

    // ── Canva template overlay coordinates (natural image size: 2480 x 1754 px) ──────────
    // These constants mark where each dynamic field sits in the template.
    // Adjust if text does not align after placing certificate-template.png in resources/templates/.
    private static final int IMG_W = 2480;
    private static final int IMG_H = 1754;

    // Recipient name — large serif italic (center-aligned on x=1240)
    private static final int NAME_CX   = 1240;
    private static final int NAME_Y    = 740;   // baseline Y in image pixels
    private static final int NAME_BOX_W = 1700;
    private static final int NAME_BOX_H = 130;

    // Course / tier label — gold text (center-aligned)
    private static final int COURSE_CX  = 1240;
    private static final int COURSE_Y   = 1010;
    private static final int COURSE_BOX_W = 1100;
    private static final int COURSE_BOX_H = 65;

    // WPM stat — left column center
    private static final int WPM_CX    = 800;
    private static final int STAT_Y    = 1148;
    private static final int STAT_BOX_W = 550;
    private static final int STAT_BOX_H = 80;

    // Accuracy stat — right column center
    private static final int ACC_CX    = 1660;

    // Issue date — bottom left (left-aligned from x)
    private static final int DATE_CX   = 555;
    private static final int DATE_Y    = 1450;
    private static final int DATE_BOX_W = 520;
    private static final int DATE_BOX_H = 55;

    // Background color of the template (cream/off-white)
    private static final float BG_R = 0.969f, BG_G = 0.961f, BG_B = 0.937f;

    private byte[] generatePdf(User user, ExamAttempt attempt, String certId) {
        String tier = attempt.getExam().getDifficultyLevel().name();
        String recipientName = (user.getFullName() != null && !user.getFullName().isBlank())
                ? user.getFullName() : user.getUsername();
        String dateStr = attempt.getCompletedAt().format(DATE_FMT);
        String wpmStr  = attempt.getWpm() + " WPM";
        String accStr  = String.format("%.1f", attempt.getAccuracy()) + "% ACC";
        String tierLabel = tier + " LEVEL";

        try (PDDocument doc = new PDDocument()) {
            // A4 landscape  — 841.89 x 595.28 pts
            PDRectangle pageSize = new PDRectangle(PDRectangle.A4.getHeight(), PDRectangle.A4.getWidth());
            PDPage page = new PDPage(pageSize);
            doc.addPage(page);

            float W = pageSize.getWidth();
            float H = pageSize.getHeight();
            float sx = W / IMG_W;   // scale x: pts per image pixel
            float sy = H / IMG_H;   // scale y: pts per image pixel (same ratio)

            // Attempt to load the Canva template PNG from classpath
            boolean hasTemplate = false;
            byte[] templateBytes = null;
            try {
                ClassPathResource tplRes = new ClassPathResource("templates/certificate-template.png");
                if (tplRes.exists()) {
                    try (InputStream is = tplRes.getInputStream()) {
                        templateBytes = is.readAllBytes();
                        hasTemplate = true;
                    }
                }
            } catch (Exception ex) {
                log.warn("Certificate template not found in classpath — falling back to text-only PDF. " +
                        "Copy certificate-template.png to backend/src/main/resources/templates/ to enable the visual template.");
            }

            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {

                if (hasTemplate) {
                    // ── 1. Draw Canva template as full-page background ──────────────────
                    PDImageXObject tpl = PDImageXObject.createFromByteArray(doc, templateBytes, "cert-template");
                    cs.drawImage(tpl, 0, 0, W, H);

                    // ── 2. Cover placeholder text with background-colored rectangles ────
                    cs.setNonStrokingColor(BG_R, BG_G, BG_B);
                    // Name box
                    cs.addRect((NAME_CX  - NAME_BOX_W / 2f) * sx,
                               H - (NAME_Y  + NAME_BOX_H / 2f) * sy,
                               NAME_BOX_W * sx, NAME_BOX_H * sy);
                    cs.fill();
                    // Course box
                    cs.addRect((COURSE_CX - COURSE_BOX_W / 2f) * sx,
                               H - (COURSE_Y + COURSE_BOX_H / 2f) * sy,
                               COURSE_BOX_W * sx, COURSE_BOX_H * sy);
                    cs.fill();
                    // WPM box
                    cs.addRect((WPM_CX - STAT_BOX_W / 2f) * sx,
                               H - (STAT_Y + STAT_BOX_H / 2f) * sy,
                               STAT_BOX_W * sx, STAT_BOX_H * sy);
                    cs.fill();
                    // Accuracy box
                    cs.addRect((ACC_CX - STAT_BOX_W / 2f) * sx,
                               H - (STAT_Y + STAT_BOX_H / 2f) * sy,
                               STAT_BOX_W * sx, STAT_BOX_H * sy);
                    cs.fill();
                    // Date box
                    cs.addRect((DATE_CX - DATE_BOX_W / 2f) * sx,
                               H - (DATE_Y + DATE_BOX_H / 2f) * sy,
                               DATE_BOX_W * sx, DATE_BOX_H * sy);
                    cs.fill();

                    // ── 3. Overlay dynamic text ─────────────────────────────────────────
                    PDType1Font nameFnt  = new PDType1Font(Standard14Fonts.FontName.TIMES_BOLD_ITALIC);
                    PDType1Font boldFnt  = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
                    PDType1Font plainFnt = new PDType1Font(Standard14Fonts.FontName.HELVETICA);

                    // Recipient name — dark navy, centered
                    float namePt = 30f;
                    float nameW  = nameFnt.getStringWidth(recipientName) / 1000f * namePt;
                    cs.setNonStrokingColor(0.106f, 0.165f, 0.290f);
                    cs.beginText();
                    cs.setFont(nameFnt, namePt);
                    cs.newLineAtOffset(W / 2f - nameW / 2f, H - NAME_Y * sy);
                    cs.showText(recipientName);
                    cs.endText();

                    // Course/tier label — gold, centered
                    float coursePt = 13f;
                    float courseW  = boldFnt.getStringWidth(tierLabel) / 1000f * coursePt;
                    cs.setNonStrokingColor(0.725f, 0.533f, 0.000f);
                    cs.beginText();
                    cs.setFont(boldFnt, coursePt);
                    cs.newLineAtOffset(W / 2f - courseW / 2f, H - COURSE_Y * sy);
                    cs.showText(tierLabel);
                    cs.endText();

                    // WPM stat — dark navy, left-column center
                    float statPt = 15f;
                    float wpmW   = boldFnt.getStringWidth(wpmStr) / 1000f * statPt;
                    float statY  = H - STAT_Y * sy;
                    cs.setNonStrokingColor(0.106f, 0.165f, 0.290f);
                    cs.beginText();
                    cs.setFont(boldFnt, statPt);
                    cs.newLineAtOffset(WPM_CX * sx - wpmW / 2f, statY);
                    cs.showText(wpmStr);
                    cs.endText();

                    // Accuracy stat — dark navy, right-column center
                    float accW = boldFnt.getStringWidth(accStr) / 1000f * statPt;
                    cs.beginText();
                    cs.setFont(boldFnt, statPt);
                    cs.newLineAtOffset(ACC_CX * sx - accW / 2f, statY);
                    cs.showText(accStr);
                    cs.endText();

                    // Date — dark navy, left-column center
                    float datePt = 10f;
                    float dateW  = plainFnt.getStringWidth(dateStr) / 1000f * datePt;
                    cs.beginText();
                    cs.setFont(plainFnt, datePt);
                    cs.newLineAtOffset(DATE_CX * sx - dateW / 2f, H - DATE_Y * sy);
                    cs.showText(dateStr);
                    cs.endText();

                    // Cert ID watermark — tiny grey at bottom
                    PDType1Font monoFnt = new PDType1Font(Standard14Fonts.FontName.COURIER);
                    float idPt = 6f;
                    String idTxt = "Certificate ID: " + certId;
                    float idW = monoFnt.getStringWidth(idTxt) / 1000f * idPt;
                    cs.setNonStrokingColor(0.65f, 0.65f, 0.65f);
                    cs.beginText();
                    cs.setFont(monoFnt, idPt);
                    cs.newLineAtOffset(W / 2f - idW / 2f, 10f);
                    cs.showText(idTxt);
                    cs.endText();

                } else {
                    // ── Fallback: plain text layout (no template image) ─────────────────
                    cs.setNonStrokingColor(0.97f, 0.97f, 1.0f);
                    cs.addRect(0, 0, W, H);
                    cs.fill();

                    PDType1Font hb = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
                    PDType1Font h  = new PDType1Font(Standard14Fonts.FontName.HELVETICA);

                    drawCentered(cs, hb, 26, "TypeMaster", 0.31f, 0.27f, 0.9f, W, H - 70);
                    drawCentered(cs, h,  13, "CERTIFICATE OF COMPLETION", 0.3f, 0.3f, 0.3f, W, H - 100);
                    drawCentered(cs, h,  12, "This is proudly presented to", 0.4f, 0.4f, 0.4f, W, H - 170);
                    drawCentered(cs, new PDType1Font(Standard14Fonts.FontName.TIMES_BOLD_ITALIC),
                                      22, recipientName, 0.1f, 0.1f, 0.1f, W, H - 210);
                    drawCentered(cs, h, 12, "for successfully completing the " + tier + " Typing Exam",
                                      0.3f, 0.3f, 0.3f, W, H - 250);
                    drawCentered(cs, hb, 14, wpmStr + "   |   " + accStr, 0.31f, 0.27f, 0.9f, W, H - 295);
                    drawCentered(cs, h,  10, "Issued: " + dateStr, 0.5f, 0.5f, 0.5f, W, H - 330);
                    drawCentered(cs, new PDType1Font(Standard14Fonts.FontName.COURIER),
                                      7, "Certificate ID: " + certId, 0.65f, 0.65f, 0.65f, W, 15);
                }
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            doc.save(out);
            return out.toByteArray();
        } catch (Exception e) {
            log.error("PDF generation failed for certId={}: {}", certId, e.getMessage());
            throw new RuntimeException("Certificate PDF generation failed", e);
        }
    }

    /** Helper: draw text centered horizontally on page at given y (PDF coordinate). */
    private void drawCentered(PDPageContentStream cs, PDType1Font font, float size,
                               String text, float r, float g, float b,
                               float pageWidth, float y) throws Exception {
        float tw = font.getStringWidth(text) / 1000f * size;
        cs.setNonStrokingColor(r, g, b);
        cs.beginText();
        cs.setFont(font, size);
        cs.newLineAtOffset((pageWidth - tw) / 2f, y);
        cs.showText(text);
        cs.endText();
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

    private CertificateDto toPublicDto(Certificate cert) {
        CertificateDto dto = new CertificateDto();
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
