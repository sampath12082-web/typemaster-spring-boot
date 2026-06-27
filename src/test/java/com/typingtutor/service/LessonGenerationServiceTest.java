package com.typingtutor.service;

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
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LessonGenerationServiceTest {

    @Mock LessonRepository lessonRepository;
    @Mock UserPerformanceRepository performanceRepository;
    @Mock UserRepository userRepository;

    @InjectMocks LessonGenerationService lessonGenerationService;

    private User user;
    private Lesson standardLesson;
    private UserPerformance passingPerf;

    @BeforeEach
    void setUp() {
        // Set @Value fields via reflection (not injected by Mockito)
        ReflectionTestUtils.setField(lessonGenerationService, "aiApiKey", "");
        ReflectionTestUtils.setField(lessonGenerationService, "aiApiUrl", "https://api.anthropic.com/v1/messages");
        ReflectionTestUtils.setField(lessonGenerationService, "aiModel", "claude-haiku-4-5-20251001");
        ReflectionTestUtils.setField(lessonGenerationService, "advancedMinWpm", 50);
        ReflectionTestUtils.setField(lessonGenerationService, "minAccuracy", 85.0);

        user = new User();
        user.setId(1L);
        user.setUsername("typist");

        standardLesson = new Lesson();
        standardLesson.setId(17L);
        standardLesson.setTitle("Advanced Punctuation");
        standardLesson.setDifficultyLevel(DifficultyLevel.ADVANCED);
        standardLesson.setDisplayOrder(17);
        standardLesson.setMinWpm(50);
        standardLesson.setMinAccuracy(85.0);
        standardLesson.setAiGenerated(false);

        passingPerf = UserPerformance.builder()
                .user(user)
                .lesson(standardLesson)
                .wpm(60)
                .accuracyPercentage(92.0)
                .build();
    }

    // ── Guard: not all standard lessons passed ─────────────────────────

    @Test
    void throwsWhenNotAllStandardLessonsPassed() {
        when(lessonRepository.countStandardLessonsByDifficultyLevel(DifficultyLevel.ADVANCED))
                .thenReturn(8L);
        when(lessonRepository.findByDifficultyLevelOrderByDisplayOrder(DifficultyLevel.ADVANCED))
                .thenReturn(List.of(standardLesson));
        when(performanceRepository.findRecentByUserIdWithLesson(1L))
                .thenReturn(List.of()); // no passing performances

        assertThatThrownBy(() -> lessonGenerationService.generateAdvancedLessonForUser(1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Complete all")
                .hasMessageContaining("0/8 passed");
    }

    @Test
    void throwsWhenOnlySomeLessonsPassed() {
        Lesson lesson2 = new Lesson();
        lesson2.setId(18L);
        lesson2.setDifficultyLevel(DifficultyLevel.ADVANCED);
        lesson2.setMinWpm(50);
        lesson2.setMinAccuracy(85.0);
        lesson2.setAiGenerated(false);

        when(lessonRepository.countStandardLessonsByDifficultyLevel(DifficultyLevel.ADVANCED))
                .thenReturn(2L);
        when(lessonRepository.findByDifficultyLevelOrderByDisplayOrder(DifficultyLevel.ADVANCED))
                .thenReturn(List.of(standardLesson, lesson2));
        // Only passed one of two
        when(performanceRepository.findRecentByUserIdWithLesson(1L))
                .thenReturn(List.of(passingPerf));

        assertThatThrownBy(() -> lessonGenerationService.generateAdvancedLessonForUser(1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("1/2 passed");
    }

    // ── Guard: user not found ──────────────────────────────────────────

    @Test
    void throwsWhenUserNotFound() {
        when(lessonRepository.countStandardLessonsByDifficultyLevel(DifficultyLevel.ADVANCED))
                .thenReturn(1L);
        when(lessonRepository.findByDifficultyLevelOrderByDisplayOrder(DifficultyLevel.ADVANCED))
                .thenReturn(List.of(standardLesson));
        when(performanceRepository.findRecentByUserIdWithLesson(1L))
                .thenReturn(List.of(passingPerf));
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> lessonGenerationService.generateAdvancedLessonForUser(1L))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("User not found");
    }

    // ── Successful generation with blank API key (placeholder path) ───

    @Test
    void generatesPlaceholderLessonWhenApiKeyBlank() {
        when(lessonRepository.countStandardLessonsByDifficultyLevel(DifficultyLevel.ADVANCED))
                .thenReturn(1L);
        when(lessonRepository.findByDifficultyLevelOrderByDisplayOrder(DifficultyLevel.ADVANCED))
                .thenReturn(List.of(standardLesson));
        when(performanceRepository.findRecentByUserIdWithLesson(1L))
                .thenReturn(List.of(passingPerf));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(lessonRepository.findMaxDisplayOrderByDifficultyLevel(DifficultyLevel.ADVANCED))
                .thenReturn(Optional.of(24));

        Lesson savedLesson = new Lesson();
        savedLesson.setId(100L);
        savedLesson.setTitle("AI-Generated: Advanced Punctuation Focus");
        savedLesson.setDifficultyLevel(DifficultyLevel.ADVANCED);
        savedLesson.setAiGenerated(true);
        when(lessonRepository.save(any(Lesson.class))).thenReturn(savedLesson);

        Lesson result = lessonGenerationService.generateAdvancedLessonForUser(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(100L);
        assertThat(result.isAiGenerated()).isTrue();
        assertThat(result.getDifficultyLevel()).isEqualTo(DifficultyLevel.ADVANCED);
    }

    @Test
    void savedLessonHasCorrectFieldsWhenApiKeyBlank() {
        when(lessonRepository.countStandardLessonsByDifficultyLevel(DifficultyLevel.ADVANCED))
                .thenReturn(1L);
        when(lessonRepository.findByDifficultyLevelOrderByDisplayOrder(DifficultyLevel.ADVANCED))
                .thenReturn(List.of(standardLesson));
        when(performanceRepository.findRecentByUserIdWithLesson(1L))
                .thenReturn(List.of(passingPerf));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(lessonRepository.findMaxDisplayOrderByDifficultyLevel(DifficultyLevel.ADVANCED))
                .thenReturn(Optional.of(24));
        when(lessonRepository.save(any(Lesson.class))).thenAnswer(inv -> inv.getArgument(0));

        lessonGenerationService.generateAdvancedLessonForUser(1L);

        ArgumentCaptor<Lesson> captor = ArgumentCaptor.forClass(Lesson.class);
        verify(lessonRepository).save(captor.capture());

        Lesson saved = captor.getValue();
        assertThat(saved.getTitle()).startsWith("AI-Generated:");
        assertThat(saved.getDifficultyLevel()).isEqualTo(DifficultyLevel.ADVANCED);
        assertThat(saved.getDisplayOrder()).isEqualTo(25); // maxOrder(24) + 1
        assertThat(saved.getMinWpm()).isEqualTo(50);
        assertThat(saved.getMinAccuracy()).isEqualTo(85.0);
        assertThat(saved.isActive()).isTrue();
        assertThat(saved.isAiGenerated()).isTrue();
        assertThat(saved.getGeneratedForUserId()).isEqualTo(1L);
        assertThat(saved.getContentText()).isNotBlank();
    }

    @Test
    void placeholderContentIsUsedWhenApiKeyBlank() {
        when(lessonRepository.countStandardLessonsByDifficultyLevel(DifficultyLevel.ADVANCED))
                .thenReturn(1L);
        when(lessonRepository.findByDifficultyLevelOrderByDisplayOrder(DifficultyLevel.ADVANCED))
                .thenReturn(List.of(standardLesson));
        when(performanceRepository.findRecentByUserIdWithLesson(1L))
                .thenReturn(List.of(passingPerf));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(lessonRepository.findMaxDisplayOrderByDifficultyLevel(DifficultyLevel.ADVANCED))
                .thenReturn(Optional.of(24));
        when(lessonRepository.save(any(Lesson.class))).thenAnswer(inv -> inv.getArgument(0));

        lessonGenerationService.generateAdvancedLessonForUser(1L);

        ArgumentCaptor<Lesson> captor = ArgumentCaptor.forClass(Lesson.class);
        verify(lessonRepository).save(captor.capture());

        // Placeholder starts with "Mastering advanced typing..."
        assertThat(captor.getValue().getContentText())
                .startsWith("Mastering advanced typing");
    }

    // ── Display order defaults to 25 when no existing max ──────────────

    @Test
    void displayOrderDefaultsWhenNoExistingMax() {
        when(lessonRepository.countStandardLessonsByDifficultyLevel(DifficultyLevel.ADVANCED))
                .thenReturn(1L);
        when(lessonRepository.findByDifficultyLevelOrderByDisplayOrder(DifficultyLevel.ADVANCED))
                .thenReturn(List.of(standardLesson));
        when(performanceRepository.findRecentByUserIdWithLesson(1L))
                .thenReturn(List.of(passingPerf));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(lessonRepository.findMaxDisplayOrderByDifficultyLevel(DifficultyLevel.ADVANCED))
                .thenReturn(Optional.empty()); // no existing lessons
        when(lessonRepository.save(any(Lesson.class))).thenAnswer(inv -> inv.getArgument(0));

        lessonGenerationService.generateAdvancedLessonForUser(1L);

        ArgumentCaptor<Lesson> captor = ArgumentCaptor.forClass(Lesson.class);
        verify(lessonRepository).save(captor.capture());
        // orElse(24) + 1 = 25
        assertThat(captor.getValue().getDisplayOrder()).isEqualTo(25);
    }

    // ── Weak area derived from lowest-accuracy performance ─────────────

    @Test
    void titleReflectsWeakestLessonArea() {
        Lesson weakLesson = new Lesson();
        weakLesson.setId(18L);
        weakLesson.setTitle("Number Row");
        weakLesson.setDifficultyLevel(DifficultyLevel.ADVANCED);
        weakLesson.setMinWpm(50);
        weakLesson.setMinAccuracy(85.0);
        weakLesson.setAiGenerated(false);

        UserPerformance weakPerf = UserPerformance.builder()
                .user(user).lesson(weakLesson).wpm(55).accuracyPercentage(70.0).build();

        when(lessonRepository.countStandardLessonsByDifficultyLevel(DifficultyLevel.ADVANCED))
                .thenReturn(2L);
        when(lessonRepository.findByDifficultyLevelOrderByDisplayOrder(DifficultyLevel.ADVANCED))
                .thenReturn(List.of(standardLesson, weakLesson));

        // Both lessons passed with qualifying scores
        UserPerformance strongPerf = UserPerformance.builder()
                .user(user).lesson(standardLesson).wpm(60).accuracyPercentage(95.0).build();
        UserPerformance weakQualifying = UserPerformance.builder()
                .user(user).lesson(weakLesson).wpm(52).accuracyPercentage(86.0).build();

        // Return performances: weakPerf has lowest accuracy overall, used for weak area detection
        when(performanceRepository.findRecentByUserIdWithLesson(1L))
                .thenReturn(List.of(strongPerf, weakQualifying, weakPerf));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(lessonRepository.findMaxDisplayOrderByDifficultyLevel(DifficultyLevel.ADVANCED))
                .thenReturn(Optional.of(24));
        when(lessonRepository.save(any(Lesson.class))).thenAnswer(inv -> inv.getArgument(0));

        lessonGenerationService.generateAdvancedLessonForUser(1L);

        ArgumentCaptor<Lesson> captor = ArgumentCaptor.forClass(Lesson.class);
        verify(lessonRepository).save(captor.capture());

        // weakPerf has lowest accuracy (70.0), its lesson title is "Number Row"
        assertThat(captor.getValue().getTitle()).contains("Number Row");
    }

    // ── Construction ───────────────────────────────────────────────────

    @Test
    void serviceConstructsWithMockedDependencies() {
        assertThat(lessonGenerationService).isNotNull();
    }
}
