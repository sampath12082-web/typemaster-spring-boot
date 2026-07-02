package com.typingtutor.controller;

import com.typingtutor.dto.InquiryDto;
import com.typingtutor.dto.InquiryRequest;
import com.typingtutor.dto.ReopenInquiryRequest;
import com.typingtutor.service.HelpAgentService;
import com.typingtutor.service.InquiryService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/inquiries")
public class InquiryController {

    private final InquiryService inquiryService;
    private final HelpAgentService helpAgentService;

    public InquiryController(InquiryService inquiryService, HelpAgentService helpAgentService) {
        this.inquiryService = inquiryService;
        this.helpAgentService = helpAgentService;
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

    @PostMapping("/chat")
    public ResponseEntity<Map<String, Object>> chat(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ChatRequest req) {
        return ResponseEntity.ok(helpAgentService.chat(req.message(), req.history()));
    }

    record ChatRequest(
        @NotBlank @Size(max = 1000) String message,
        List<Map<String, String>> history
    ) {}

    /** Feature E — reopen a resolved inquiry (max 3 times). */
    @PostMapping("/{id}/reopen")
    public ResponseEntity<InquiryDto> reopen(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ReopenInquiryRequest req) {
        return ResponseEntity.ok(
            inquiryService.reopenInquiry(id, userDetails.getUsername(), req.getReason()));
    }
}
