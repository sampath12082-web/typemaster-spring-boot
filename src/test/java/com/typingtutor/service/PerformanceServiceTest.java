package com.typingtutor.service;

import com.typingtutor.dto.PerformanceDto;
import com.typingtutor.dto.PerformanceRequest;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PerformanceServiceTest {

    @Mock UserPerformanceRepository performanceRepository;
    @Mock UserRepository userRepository;
    @Mock LessonRepository lessonRepository;
    @Mock LessonService lessonService;
    @Mock AuditLogService auditLogService;

    @InjectMocks PerformanceService performanceService;

    private User user;
    private Lesson lesson;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .username("student")
                .email("student@example.com")
                .password("encoded")
                .build();
        user.setId(1L);

        lesson = new Lesson();
        lesson.setId(5L);
        lesson.setTitle("Home Row Keys");
        lesson.setDifficultyLevel(DifficultyLevel.BASIC);
        lesson.setDisplayOrder(1);
        lesson.setMinWpm(20);
        lesson.setMinAccuracy(85.0);
        lesson.setContentText("asdf jkl;");
    }

    // --- savePerformance success ---

    @Test
    void savePerformance_success_returnsDtoAndLogsAudit() {
        PerformanceRequest request = new PerformanceRequest();
        request.setLessonId(5L);
        request.setWpm(45);
        request.setAccuracyPercentage(92.5);

        when(userRepository.findByUsername("student")).thenReturn(Optional.of(user));
        when(lessonRepository.findById(5L)).thenReturn(Optional.of(lesson));
        when(lessonService.computeLessonStatus(1L, lesson)).thenReturn("AVAILABLE");

        UserPerformance savedPerf = UserPerformance.builder()
                .user(user).lesson(lesson).wpm(45).accuracyPercentage(92.5).build();
        // Simulate the DB setting the id
        when(performanceRepository.save(any(UserPerformance.class))).thenAnswer(inv -> {
            UserPerformance p = inv.getArgument(0);
            // Verify the fields were set correctly on the entity being saved
            assertThat(p.getUser()).isEqualTo(user);
            assertThat(p.getLesson()).isEqualTo(lesson);
            assertThat(p.getWpm()).isEqualTo(45);
            assertThat(p.getAccuracyPercentage()).isEqualTo(92.5);
            return savedPerf;
        });

        PerformanceDto result = performanceService.savePerformance(request, "student");

        assertThat(result.getLessonId()).isEqualTo(5L);
        assertThat(result.getLessonTitle()).isEqualTo("Home Row Keys");
        assertThat(result.getWpm()).isEqualTo(45);
        assertThat(result.getAccuracyPercentage()).isEqualTo(92.5);

        verify(performanceRepository).save(any(UserPerformance.class));
        verify(auditLogService).log(eq("student"), eq("LESSON_COMPLETED"), anyString());
    }

    @Test
    void savePerformance_passedLesson_alsoWorks() {
        PerformanceRequest request = new PerformanceRequest();
        request.setLessonId(5L);
        request.setWpm(30);
        request.setAccuracyPercentage(90.0);

        when(userRepository.findByUsername("student")).thenReturn(Optional.of(user));
        when(lessonRepository.findById(5L)).thenReturn(Optional.of(lesson));
        when(lessonService.computeLessonStatus(1L, lesson)).thenReturn("PASSED");

        UserPerformance savedPerf = UserPerformance.builder()
                .user(user).lesson(lesson).wpm(30).accuracyPercentage(90.0).build();
        when(performanceRepository.save(any(UserPerformance.class))).thenReturn(savedPerf);

        PerformanceDto result = performanceService.savePerformance(request, "student");

        assertThat(result.getWpm()).isEqualTo(30);
        verify(performanceRepository).save(any(UserPerformance.class));
    }

    // --- savePerformance locked lesson ---

    @Test
    void savePerformance_lockedLesson_throwsLessonLockedException() {
        PerformanceRequest request = new PerformanceRequest();
        request.setLessonId(5L);
        request.setWpm(45);
        request.setAccuracyPercentage(92.5);

        when(userRepository.findByUsername("student")).thenReturn(Optional.of(user));
        when(lessonRepository.findById(5L)).thenReturn(Optional.of(lesson));
        when(lessonService.computeLessonStatus(1L, lesson)).thenReturn("LOCKED");

        assertThatThrownBy(() -> performanceService.savePerformance(request, "student"))
                .isInstanceOf(PerformanceService.LessonLockedException.class)
                .hasMessageContaining("Lesson is locked");

        verify(performanceRepository, never()).save(any());
        verify(auditLogService, never()).log(anyString(), anyString(), anyString());
    }

    // --- savePerformance user not found ---

    @Test
    void savePerformance_userNotFound_throws() {
        PerformanceRequest request = new PerformanceRequest();
        request.setLessonId(5L);
        request.setWpm(45);
        request.setAccuracyPercentage(92.5);

        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> performanceService.savePerformance(request, "unknown"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User not found");

        verify(performanceRepository, never()).save(any());
    }

    // --- savePerformance lesson not found ---

    @Test
    void savePerformance_lessonNotFound_throws() {
        PerformanceRequest request = new PerformanceRequest();
        request.setLessonId(999L);
        request.setWpm(45);
        request.setAccuracyPercentage(92.5);

        when(userRepository.findByUsername("student")).thenReturn(Optional.of(user));
        when(lessonRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> performanceService.savePerformance(request, "student"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Lesson not found");
    }

    // --- getUserHistory ---

    @Test
    void getUserHistory_returnsListOfPerformanceDto() {
        when(userRepository.findByUsername("student")).thenReturn(Optional.of(user));

        Lesson lesson2 = new Lesson();
        lesson2.setId(6L);
        lesson2.setTitle("Top Row");
        lesson2.setDifficultyLevel(DifficultyLevel.BASIC);

        UserPerformance perf1 = UserPerformance.builder()
                .user(user).lesson(lesson).wpm(45).accuracyPercentage(92.5).build();
        UserPerformance perf2 = UserPerformance.builder()
                .user(user).lesson(lesson2).wpm(50).accuracyPercentage(95.0).build();

        when(performanceRepository.findByUserIdOrderByCompletedAtDesc(1L))
                .thenReturn(List.of(perf2, perf1));

        List<PerformanceDto> result = performanceService.getUserHistory("student");

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getLessonTitle()).isEqualTo("Top Row");
        assertThat(result.get(0).getWpm()).isEqualTo(50);
        assertThat(result.get(1).getLessonTitle()).isEqualTo("Home Row Keys");
        assertThat(result.get(1).getWpm()).isEqualTo(45);
    }

    @Test
    void getUserHistory_emptyHistory_returnsEmptyList() {
        when(userRepository.findByUsername("student")).thenReturn(Optional.of(user));
        when(performanceRepository.findByUserIdOrderByCompletedAtDesc(1L))
                .thenReturn(List.of());

        List<PerformanceDto> result = performanceService.getUserHistory("student");

        assertThat(result).isEmpty();
    }

    @Test
    void getUserHistory_userNotFound_throws() {
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> performanceService.getUserHistory("ghost"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User not found");
    }
}
