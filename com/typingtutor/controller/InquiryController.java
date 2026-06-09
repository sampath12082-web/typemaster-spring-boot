package com.typingtutor.controller;

import com.typingtutor.dto.InquiryDto;
import com.typingtutor.dto.InquiryRequest;
import com.typingtutor.service.InquiryService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inquiries")
public class InquiryController {

    private final InquiryService inquiryService;

    public InquiryController(InquiryService inquiryService) {
        this.inquiryService = inquiryService;
    }

    @PostMapping
    public ResponseEntity<InquiryDto> submit(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody InquiryRequest req) {
        return ResponseEntity.ok(inquiryService.submitInquiry(
            userDetails.getUsername(), req.getSubject(), req.getMessage()));
    }

    @GetMapping("/mine")
    public ResponseEntity<List<InquiryDto>> mine(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(inquiryService.getMyInquiries(userDetails.getUsername()));
    }
}
