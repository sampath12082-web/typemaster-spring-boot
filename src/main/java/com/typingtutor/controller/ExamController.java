package com.typingtutor.controller;

import com.typingtutor.dto.CertificateDto;
import com.typingtutor.dto.ExamAttemptDto;
import com.typingtutor.dto.ExamDto;
import com.typingtutor.dto.ExamStatusDto;
import com.typingtutor.dto.ExamSubmitRequest;
import com.typingtutor.security.UserPrincipal;
import com.typingtutor.service.CertificateService;
import com.typingtutor.service.ExamService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class ExamController {

    private final ExamService examService;
    private final CertificateService certificateService;

    public ExamController(ExamService examService, CertificateService certificateService) {
        this.examService = examService;
        this.certificateService = certificateService;
    }

    @GetMapping("/api/exams/status")
    public ResponseEntity<List<ExamStatusDto>> getMyExamStatuses(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(examService.getMyExamStatuses(userDetails.getUsername()));
    }

    @GetMapping("/api/exams/{tier}")
    public ResponseEntity<ExamDto> getExam(
            @PathVariable String tier,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(examService.getExam(tier, userDetails.getUsername()));
    }

    @PostMapping("/api/exams/{tier}/submit")
    public ResponseEntity<ExamAttemptDto> submitExam(
            @PathVariable String tier,
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ExamSubmitRequest req) {
        return ResponseEntity.ok(examService.submitExam(
            tier, userDetails.getUsername(), req.getWpm(), req.getAccuracy(), req.getTimeTaken()));
    }

    @GetMapping("/api/certificates")
    public ResponseEntity<List<CertificateDto>> getMyCertificates(
            @AuthenticationPrincipal UserPrincipal userDetails) {
        return ResponseEntity.ok(certificateService.getUserCertificates(userDetails.getUser().getId()));
    }

    /** Public endpoint — no auth required (permitted in SecurityConfig). */
    @GetMapping("/api/certificates/{certId}")
    public ResponseEntity<CertificateDto> verifyCertificate(@PathVariable String certId) {
        return ResponseEntity.ok(certificateService.getCertificateByPublicId(certId));
    }

    @GetMapping("/api/certificates/{certId}/pdf")
    public ResponseEntity<byte[]> downloadPdf(@PathVariable String certId) {
        byte[] pdf = certificateService.getCertificatePdf(certId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"TypeMaster_Certificate_" + certId + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}
