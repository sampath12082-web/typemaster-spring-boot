package com.typingtutor.service;

import com.typingtutor.dto.LessonWithStatusDto;
import com.typingtutor.entity.DifficultyLevel;
import com.typingtutor.entity.Lesson;
import com.typingtutor.entity.User;
import com.typingtutor.entity.UserPerformance;
import com.typingtutor.repository.LessonRepository;
import com.typingtutor.repository.UserPerformanceRepository;
import com.typingtutor.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LessonServiceTest {

    @Mock LessonRepository lessonRepository;
    @Mock UserPerformanceRepository performanceRepository;
    @Mock UserRepository userRepository;

    @InjectMocks LessonService lessonService;

    private User user;
    private Lesson lesson1;
    private Lesson lesson2;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("testuser");

        lesson1 = new Lesson();
        lesson1.setId(10L);
        lesson1.setTitle("Lesson 1");
        lesson1.setDifficultyLevel(DifficultyLevel.BASIC);
        lesson1.setDisplayOrder(1);
        lesson1.setMinWpm(20);
        lesson1.setMinAccuracy(85.0);
        lesson1.setContentText("aaa bbb ccc");

        lesson2 = new Lesson();
        lesson2.setId(11L);
        lesson2.setTitle("Lesson 2");
        lesson2.setDifficultyLevel(DifficultyLevel.BASIC);
        lesson2.setDisplayOrder(2);
        lesson2.setMinWpm(20);
        lesson2.setMinAccuracy(85.0);
        lesson2.setContentText("ddd eee fff");
    }

    @Test
    void firstLessonInTierIsAvailableForNewUser() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(lessonRepository.findAllByOrderByDifficultyLevelAscDisplayOrderAsc())
                .thenReturn(List.of(lesson1, lesson2));
        when(performanceRepository.findRecentByUserIdWithLesson(1L)).thenReturn(List.of());

        List<LessonWithStatusDto> result = lessonService.getAllLessonsForUser("testuser");

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getStatus()).isEqualTo("AVAILABLE");
        assertThat(result.get(1).getStatus()).isEqualTo("LOCKED");
    }

    @Test
    void secondLessonUnlocksAfterFirstIsPassed() {
        UserPerformance passing = UserPerformance.builder()
                .user(user).lesson(lesson1).wpm(25).accuracyPercentage(90.0).build();

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(lessonRepository.findAllByOrderByDifficultyLevelAscDisplayOrderAsc())
                .thenReturn(List.of(lesson1, lesson2));
        when(performanceRepository.findRecentByUserIdWithLesson(1L)).thenReturn(List.of(passing));

        List<LessonWithStatusDto> result = lessonService.getAllLessonsForUser("testuser");

        assertThat(result.get(0).getStatus()).isEqualTo("PASSED");
        assertThat(result.get(1).getStatus()).isEqualTo("AVAILABLE");
    }

    @Test
    void failedAttemptOnFirstLessonShowsCorrectStatus() {
        UserPerformance failing = UserPerformance.builder()
                .user(user).lesson(lesson1).wpm(10).accuracyPercentage(70.0).build();

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(lessonRepository.findAllByOrderByDifficultyLevelAscDisplayOrderAsc())
                .thenReturn(List.of(lesson1, lesson2));
        when(performanceRepository.findRecentByUserIdWithLesson(1L)).thenReturn(List.of(failing));

        List<LessonWithStatusDto> result = lessonService.getAllLessonsForUser("testuser");

        assertThat(result.get(0).getStatus()).isEqualTo("FAILED_ATTEMPT");
        assertThat(result.get(1).getStatus()).isEqualTo("LOCKED");
    }

    @Test
    void lessonDtoContainsMinThresholds() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(lessonRepository.findAllByOrderByDifficultyLevelAscDisplayOrderAsc())
                .thenReturn(List.of(lesson1));
        when(performanceRepository.findRecentByUserIdWithLesson(1L)).thenReturn(List.of());

        List<LessonWithStatusDto> result = lessonService.getAllLessonsForUser("testuser");

        assertThat(result.get(0).getMinWpm()).isEqualTo(20);
        assertThat(result.get(0).getMinAccuracy()).isEqualTo(85.0);
    }
}
