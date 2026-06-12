package com.typingtutor.service;

import com.typingtutor.dto.LessonWithStatusDto;
import com.typingtutor.entity.DifficultyLevel;
import com.typingtutor.entity.Lesson;
import com.typingtutor.entity.UserPerformance;
import com.typingtutor.repository.LessonRepository;
import com.typingtutor.repository.UserPerformanceRepository;
import com.typingtutor.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class LessonService {

    private static final Logger log = LoggerFactory.getLogger(LessonService.class);

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

    public List<LessonWithStatusDto> getAllLessonsForUser(String username) {
        Long userId = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found")).getId();

        List<Lesson> allLessons = lessonRepository.findAllByOrderByDifficultyLevelAscDisplayOrderAsc();

        // Pre-load all user performances to avoid N+1
        List<UserPerformance> allPerfs = performanceRepository.findRecentByUserIdWithLesson(userId);
        Map<Long, List<UserPerformance>> perfsByLesson = new HashMap<>();
        for (UserPerformance p : allPerfs) {
            perfsByLesson.computeIfAbsent(p.getLesson().getId(), k -> new ArrayList<>()).add(p);
        }

        // Find minimum display_order per tier (= first lesson in tier)
        Map<DifficultyLevel, Integer> tierMinOrder = new HashMap<>();
        for (Lesson l : allLessons) {
            tierMinOrder.merge(l.getDifficultyLevel(), l.getDisplayOrder(), Math::min);
        }

        return allLessons.stream()
                .map(lesson -> toDto(lesson, perfsByLesson, tierMinOrder, allLessons))
                .collect(Collectors.toList());
    }

    public LessonWithStatusDto getLessonById(Long lessonId, String username) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new IllegalArgumentException("Lesson not found"));
        Long userId = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found")).getId();

        List<Lesson> allLessons = lessonRepository.findAllByOrderByDifficultyLevelAscDisplayOrderAsc();
        List<UserPerformance> allPerfs = performanceRepository.findRecentByUserIdWithLesson(userId);
        Map<Long, List<UserPerformance>> perfsByLesson = new HashMap<>();
        for (UserPerformance p : allPerfs) {
            perfsByLesson.computeIfAbsent(p.getLesson().getId(), k -> new ArrayList<>()).add(p);
        }
        Map<DifficultyLevel, Integer> tierMinOrder = new HashMap<>();
        for (Lesson l : allLessons) {
            tierMinOrder.merge(l.getDifficultyLevel(), l.getDisplayOrder(), Math::min);
        }
        return toDto(lesson, perfsByLesson, tierMinOrder, allLessons);
    }

    public String computeLessonStatus(Long userId, Lesson lesson) {
        List<Lesson> allLessons = lessonRepository.findAllByOrderByDifficultyLevelAscDisplayOrderAsc();
        List<UserPerformance> allPerfs = performanceRepository.findRecentByUserIdWithLesson(userId);
        Map<Long, List<UserPerformance>> perfsByLesson = new HashMap<>();
        for (UserPerformance p : allPerfs) {
            perfsByLesson.computeIfAbsent(p.getLesson().getId(), k -> new ArrayList<>()).add(p);
        }
        Map<DifficultyLevel, Integer> tierMinOrder = new HashMap<>();
        for (Lesson l : allLessons) {
            tierMinOrder.merge(l.getDifficultyLevel(), l.getDisplayOrder(), Math::min);
        }
        return computeStatus(lesson, perfsByLesson, tierMinOrder, allLessons);
    }

    private LessonWithStatusDto toDto(Lesson lesson,
                                      Map<Long, List<UserPerformance>> perfsByLesson,
                                      Map<DifficultyLevel, Integer> tierMinOrder,
                                      List<Lesson> allLessons) {
        List<UserPerformance> perfs = perfsByLesson.getOrDefault(lesson.getId(), Collections.emptyList());
        String status = computeStatus(lesson, perfsByLesson, tierMinOrder, allLessons);

        LessonWithStatusDto dto = new LessonWithStatusDto();
        dto.setId(lesson.getId());
        dto.setTitle(lesson.getTitle());
        dto.setDifficultyLevel(lesson.getDifficultyLevel().name());
        dto.setContentText(lesson.getContentText());
        dto.setDisplayOrder(lesson.getDisplayOrder());
        dto.setMinWpm(lesson.getMinWpm());
        dto.setMinAccuracy(lesson.getMinAccuracy());
        dto.setStatus(status);

        // Best performance = highest WPM in list (sorted DESC from query)
        if (!perfs.isEmpty()) {
            perfs.stream().mapToInt(UserPerformance::getWpm).max()
                    .ifPresent(w -> dto.setBestWpm(w));
            perfs.stream().mapToDouble(UserPerformance::getAccuracyPercentage).max()
                    .ifPresent(a -> dto.setBestAccuracy(a));
        }
        return dto;
    }

    private String computeStatus(Lesson lesson,
                                  Map<Long, List<UserPerformance>> perfsByLesson,
                                  Map<DifficultyLevel, Integer> tierMinOrder,
                                  List<Lesson> allLessons) {
        List<UserPerformance> perfs = perfsByLesson.getOrDefault(lesson.getId(), Collections.emptyList());

        boolean passed = perfs.stream().anyMatch(p ->
                p.getWpm() >= lesson.getMinWpm() && p.getAccuracyPercentage() >= lesson.getMinAccuracy());
        if (passed) return "PASSED";

        Integer minOrderForTier = tierMinOrder.get(lesson.getDifficultyLevel());
        boolean isFirstInTier = lesson.getDisplayOrder().equals(minOrderForTier);

        if (isFirstInTier) {
            return perfs.isEmpty() ? "AVAILABLE" : "FAILED_ATTEMPT";
        }

        // Find previous lesson in same tier
        Optional<Lesson> prev = allLessons.stream()
                .filter(l -> l.getDifficultyLevel() == lesson.getDifficultyLevel()
                        && l.getDisplayOrder() == lesson.getDisplayOrder() - 1)
                .findFirst();

        if (prev.isEmpty()) return "LOCKED";

        Lesson prevLesson = prev.get();
        List<UserPerformance> prevPerfs = perfsByLesson.getOrDefault(prevLesson.getId(), Collections.emptyList());
        boolean prevPassed = prevPerfs.stream().anyMatch(p ->
                p.getWpm() >= prevLesson.getMinWpm() && p.getAccuracyPercentage() >= prevLesson.getMinAccuracy());

        if (prevPassed) {
            return perfs.isEmpty() ? "AVAILABLE" : "FAILED_ATTEMPT";
        }
        return "LOCKED";
    }
}
