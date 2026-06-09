package com.typingtutor.service;

import com.typingtutor.dto.PerformanceDto;
import com.typingtutor.dto.PerformanceRequest;
import com.typingtutor.entity.Lesson;
import com.typingtutor.entity.User;
import com.typingtutor.entity.UserPerformance;
import com.typingtutor.repository.LessonRepository;
import com.typingtutor.repository.UserPerformanceRepository;
import com.typingtutor.repository.UserRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PerformanceService {

    private final UserPerformanceRepository performanceRepository;
    private final UserRepository userRepository;
    private final LessonRepository lessonRepository;

    public PerformanceService(UserPerformanceRepository performanceRepository,
                              UserRepository userRepository,
                              LessonRepository lessonRepository) {
        this.performanceRepository = performanceRepository;
        this.userRepository = userRepository;
        this.lessonRepository = lessonRepository;
    }

    public PerformanceDto savePerformance(PerformanceRequest request, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        Lesson lesson = lessonRepository.findById(request.getLessonId())
                .orElseThrow(() -> new IllegalArgumentException("Lesson not found"));
        UserPerformance performance = UserPerformance.builder()
                .user(user).lesson(lesson)
                .wpm(request.getWpm())
                .accuracyPercentage(request.getAccuracyPercentage())
                .build();
        performance = performanceRepository.save(performance);
        return toDto(performance);
    }

    public List<PerformanceDto> getUserHistory(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return performanceRepository.findByUserIdOrderByCompletedAtDesc(user.getId())
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    private PerformanceDto toDto(UserPerformance p) {
        PerformanceDto dto = new PerformanceDto();
        dto.setId(p.getId());
        dto.setLessonId(p.getLesson().getId());
        dto.setLessonTitle(p.getLesson().getTitle());
        dto.setWpm(p.getWpm());
        dto.setAccuracyPercentage(p.getAccuracyPercentage());
        dto.setCompletedAt(p.getCompletedAt());
        return dto;
    }
}
