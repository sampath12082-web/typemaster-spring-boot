package com.typingtutor.service;

import com.typingtutor.dto.PlacementResultDto;
import com.typingtutor.entity.DifficultyLevel;
import com.typingtutor.entity.Lesson;
import com.typingtutor.entity.User;
import com.typingtutor.repository.LessonRepository;
import com.typingtutor.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@Service
public class PlacementService {

    private static final Logger log = LoggerFactory.getLogger(PlacementService.class);

    @Value("${app.placement.passage:The quick brown fox jumps over the lazy dog. Pack my box with five dozen liquor jugs. How vexingly quick daft zebras jump. The five boxing wizards jump quickly.}")
    private String placementPassage;

    @Value("${app.placement.time-seconds:60}")
    private int timeLimitSeconds;

    private final UserRepository userRepository;
    private final LessonRepository lessonRepository;

    public PlacementService(UserRepository userRepository, LessonRepository lessonRepository) {
        this.userRepository = userRepository;
        this.lessonRepository = lessonRepository;
    }

    public Map<String, Object> getPlacementTest() {
        return Map.of(
            "passage", placementPassage,
            "timeLimitSeconds", timeLimitSeconds
        );
    }

    @Transactional
    public PlacementResultDto submitPlacement(String username, int wpm, double accuracy) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NoSuchElementException("User not found"));

        String tier = determineTier(wpm);
        DifficultyLevel level = DifficultyLevel.valueOf(tier);

        List<Lesson> tierLessons = lessonRepository.findByDifficultyLevelOrderByDisplayOrder(level);
        Long startLessonId = tierLessons.isEmpty() ? null : tierLessons.get(0).getId();

        user.setPlacementCompleted(true);
        user.setRecommendedTier(tier);
        user.setPlacementWpm(wpm);
        userRepository.save(user);

        log.debug("Placement submitted for user={} wpm={} tier={}", username, wpm, tier);
        return new PlacementResultDto(tier, startLessonId, wpm, accuracy);
    }

    private String determineTier(int wpm) {
        if (wpm < 20) return "BASIC";
        if (wpm < 40) return "INTERMEDIATE";
        return "ADVANCED";
    }
}
