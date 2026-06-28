package com.typingtutor.controller;

import com.typingtutor.dto.PlacementResultDto;
import com.typingtutor.service.PlacementService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/placement")
public class PlacementController {

    private final PlacementService placementService;

    public PlacementController(PlacementService placementService) {
        this.placementService = placementService;
    }

    @GetMapping("/test")
    public ResponseEntity<Map<String, Object>> getTest() {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(3600, TimeUnit.SECONDS).cachePublic())
                .body(placementService.getPlacementTest());
    }

    @PostMapping("/submit")
    public ResponseEntity<PlacementResultDto> submit(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody PlacementSubmitBody body) {
        return ResponseEntity.ok(
            placementService.submitPlacement(userDetails.getUsername(), body.wpm(), body.accuracy()));
    }

    @PostMapping("/skip")
    public ResponseEntity<PlacementResultDto> skip(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
            placementService.skipPlacement(userDetails.getUsername()));
    }

    record PlacementSubmitBody(
            @NotNull @Min(0) Integer wpm,
            @NotNull @DecimalMin("0.0") @DecimalMax("100.0") Double accuracy) {}
}
