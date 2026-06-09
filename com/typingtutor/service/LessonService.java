package com.typingtutor.service;

import com.typingtutor.dto.LessonDto;
import com.typingtutor.entity.Lesson;
import com.typingtutor.entity.UserPerformance;
import com.typingtutor.repository.LessonRepository;
import com.typingtutor.repository.UserPerformanceRepository;
import com.typingtutor.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class LessonService {

    private final LessonRepository lessonRepository;
    private final UserPerformanceRepository performanceRepository;
    private final UserRepository userRepository;

    public LessonService(LessonRepository lessonRepository,
                         UserPerformanceRepository performanceRepository,
                         UserRepository userRepository) {
        this.lessonRepository = lessonRepository;
        this.performanceRepository = performanceRepository;
        this.userRepository = userRepository;
    }

    public List<LessonDto> getAllLessonsForUser(String username) {
        Long userId = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found")).getId();
        Set<Long> completedIds = performanceRepository
                .findCompletedLessonIdsByUserId(userId).stream().collect(Collectors.toSet());
        return lessonRepository.findAllByOrderByDifficultyLevelAscDisplayOrderAsc()
                .stream()
                .map(lesson -> toDto(lesson, userId, completedIds.contains(lesson.getId())))
                .collect(Collectors.toList());
    }

    public LessonDto getLessonById(Long lessonId, String username) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new IllegalArgumentException("Lesson not found"));
        Long userId = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found")).getId();
        boolean completed = !performanceRepository.findAllByUserIdAndLessonId(userId, lessonId).isEmpty();
        return toDto(lesson, userId, completed);
    }

    private LessonDto toDto(Lesson lesson, Long userId, boolean completed) {
        LessonDto dto = new LessonDto();
        dto.setId(lesson.getId());
        dto.setTitle(lesson.getTitle());
        dto.setDifficultyLevel(lesson.getDifficultyLevel());
        dto.setContentText(lesson.getContentText());
        dto.setDisplayOrder(lesson.getDisplayOrder());
        dto.setCompleted(completed);

        // Best performance = first element (query is ORDER BY wpm DESC)
        List<UserPerformance> perfs = performanceRepository.findAllByUserIdAndLessonId(userId, lesson.getId());
        if (!perfs.isEmpty()) {
            dto.setBestWpm(perfs.get(0).getWpm());
            dto.setBestAccuracy(perfs.get(0).getAccuracyPercentage());
        }
        return dto;
    }
}
