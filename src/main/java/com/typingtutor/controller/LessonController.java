package com.typingtutor.controller;

import com.typingtutor.dto.LessonWithStatusDto;
import com.typingtutor.entity.Lesson;
import com.typingtutor.security.UserPrincipal;
import com.typingtutor.service.LessonGenerationService;
import com.typingtutor.service.LessonService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/lessons")
public class LessonController {

    private final LessonService lessonService;
    private final LessonGenerationService lessonGenerationService;

    public LessonController(LessonService lessonService,
                            LessonGenerationService lessonGenerationService) {
        this.lessonService = lessonService;
        this.lessonGenerationService = lessonGenerationService;
    }

    @GetMapping
    public ResponseEntity<List<LessonWithStatusDto>> getAllLessons(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(lessonService.getAllLessonsForUser(userDetails.getUsername()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<LessonWithStatusDto> getLesson(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(lessonService.getLessonById(id, userDetails.getUsername()));
    }

    /** Feature F — generate next AI lesson once all standard ADVANCED lessons are complete. */
    @PostMapping("/generate-next")
    public ResponseEntity<?> generateNext(@AuthenticationPrincipal UserPrincipal userDetails) {
        Lesson generated = lessonGenerationService.generateAdvancedLessonForUser(userDetails.getUser().getId());
        return ResponseEntity.accepted().body(Map.of(
            "message", "New AI lesson generated successfully.",
            "lessonId", generated.getId(),
            "title", generated.getTitle()
        ));
    }
}
