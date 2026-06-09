package com.typingtutor.controller;

import com.typingtutor.dto.LessonDto;
import com.typingtutor.service.LessonService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/lessons")
public class LessonController {

    private final LessonService lessonService;

    public LessonController(LessonService lessonService) {
        this.lessonService = lessonService;
    }

    @GetMapping
    public ResponseEntity<List<LessonDto>> getAllLessons(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(lessonService.getAllLessonsForUser(userDetails.getUsername()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<LessonDto> getLesson(@PathVariable Long id,
                                               @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(lessonService.getLessonById(id, userDetails.getUsername()));
    }
}
