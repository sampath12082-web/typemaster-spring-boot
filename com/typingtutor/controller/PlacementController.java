package com.typingtutor.controller;

import com.typingtutor.dto.PlacementResultDto;
import com.typingtutor.service.PlacementService;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/placement")
public class PlacementController {

    private final PlacementService placementService;

    public PlacementController(PlacementService placementService) {
        this.placementService = placementService;
    }

    @GetMapping("/test")
    public ResponseEntity<Map<String, Object>> getTest() {
        return ResponseEntity.ok(placementService.getPlacementTest());
    }

    @PostMapping("/submit")
    public ResponseEntity<PlacementResultDto> submit(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody PlacementSubmitBody body) {
        return ResponseEntity.ok(
            placementService.submitPlacement(userDetails.getUsername(), body.wpm(), body.accuracy()));
    }

    record PlacementSubmitBody(@NotNull @Min(0) Integer wpm, @NotNull Double accuracy) {}
}
