package com.typingtutor.controller;

import com.typingtutor.dto.PerformanceDto;
import com.typingtutor.dto.PerformanceRequest;
import com.typingtutor.service.PerformanceService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/performance")
public class PerformanceController {

    private final PerformanceService performanceService;

    public PerformanceController(PerformanceService performanceService) {
        this.performanceService = performanceService;
    }

    @PostMapping
    public ResponseEntity<PerformanceDto> savePerformance(
            @Valid @RequestBody PerformanceRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(performanceService.savePerformance(request, userDetails.getUsername()));
    }

    @GetMapping("/history")
    public ResponseEntity<List<PerformanceDto>> getHistory(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(performanceService.getUserHistory(userDetails.getUsername()));
    }
}
